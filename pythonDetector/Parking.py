import cv2
import pickle
import numpy as np
import os
import time
import firebase_admin
from firebase_admin import credentials, db

cred = credentials.Certificate(
    r"H:\Documents\parking-1034e-firebase-adminsdk-fbsvc-1c5010e464.json"
)

firebase_admin.initialize_app(cred, {
    "databaseURL": "https://parking-1034e-default-rtdb.europe-west1.firebasedatabase.app"
})

VIDEO_PATH = r"H:\Downloads\Carparkvid.mp4"
SLOTS_FILE = "parking_slots.pkl"
LOCATION_ID = "loc1"

if not os.path.exists(SLOTS_FILE):
    print("No slots found! Draw slots first.")
    exit()

slots = pickle.load(open(SLOTS_FILE, "rb"))

cap = cv2.VideoCapture(VIDEO_PATH)
fgbg = cv2.createBackgroundSubtractorMOG2(
    history=100,
    varThreshold=50,
    detectShadows=False
)

ret, frame = cap.read()
if not ret:
    print("Cannot read video.")
    exit()

prev_states = [False] * len(slots)
last_update = 0

def slot_mask(frame_gray, slot):
    mask = np.zeros(frame_gray.shape, np.uint8)
    cv2.fillPoly(mask, [np.array(slot, np.int32).reshape((-1, 1, 2))], 255)
    return mask

while True:
    ret, frame = cap.read()

    if not ret:
        cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
        continue

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    fgmask = fgbg.apply(frame)

    free_count = 0
    firebase_slots = {}

    for i, slot in enumerate(slots):
        mask = slot_mask(gray, slot)
        pixels = cv2.countNonZero(cv2.bitwise_and(fgmask, fgmask, mask=mask))

        occupied = pixels > 50

        slot_name = f"A{i + 1}"

        # true = available, false = occupied
        firebase_slots[slot_name] = not occupied

        if occupied != prev_states[i]:
            print(f"{slot_name} {'OCCUPIED' if occupied else 'FREE'}")
            prev_states[i] = occupied

        color = (0, 0, 255) if occupied else (0, 255, 0)
        label = "X" if occupied else slot_name

        pts = np.array(slot, np.int32).reshape((-1, 1, 2))
        cv2.polylines(frame, [pts], True, color, 2)

        cx = int(np.mean([p[0] for p in slot]))
        cy = int(np.mean([p[1] for p in slot]))

        cv2.putText(
            frame,
            label,
            (cx - 15, cy + 5),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.5,
            color,
            1
        )

        if not occupied:
            free_count += 1

    # Update Firebase every 2 seconds
    if time.time() - last_update > 2:
        ref = db.reference()

        ref.update({
            f"spaces/{LOCATION_ID}": firebase_slots,
            f"locations/{LOCATION_ID}/available": free_count,
            f"locations/{LOCATION_ID}/total": len(slots)
        })

        print("Firebase updated:", firebase_slots)
        last_update = time.time()

    cv2.putText(
        frame,
        f"Free: {free_count} Occupied: {len(slots) - free_count} Total: {len(slots)}",
        (10, 30),
        cv2.FONT_HERSHEY_SIMPLEX,
        0.7,
        (255, 255, 255),
        2
    )

    cv2.imshow("Parking", frame)

    if cv2.waitKey(25) & 0xFF == ord("q"):
        break

cap.release()
cv2.destroyAllWindows()