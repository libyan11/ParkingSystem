import cv2
import pickle
import os

VIDEO_PATH = r"H:\Downloads\Carparkvid.mp4"
SLOTS_FILE = "parking_slots.pkl"

slots = []
current_points = []

cap = cv2.VideoCapture(VIDEO_PATH)
ret, frame = cap.read()

if not ret:
    print("Could not read video.")
    exit()

display_frame = frame.copy()

def redraw():
    global display_frame
    display_frame = frame.copy()

    for slot in slots:
        pts = []
        for point in slot:
            pts.append(point)

        for point in pts:
            cv2.circle(display_frame, point, 5, (0, 255, 0), -1)

        for i in range(len(pts)):
            cv2.line(display_frame, pts[i], pts[(i + 1) % len(pts)], (0, 255, 0), 2)

    for point in current_points:
        cv2.circle(display_frame, point, 5, (0, 0, 255), -1)

def mouse_click(event, x, y, flags, param):
    global current_points

    if event == cv2.EVENT_LBUTTONDOWN:
        current_points.append((x, y))
        print(f"Point added: {(x, y)}")

        if len(current_points) == 4:
            slots.append(current_points.copy())
            print(f"Slot {len(slots)} added")
            current_points.clear()

        redraw()

cv2.namedWindow("Draw Parking Slots")
cv2.setMouseCallback("Draw Parking Slots", mouse_click)

redraw()

print("Instructions:")
print("Click 4 corners of each parking space.")
print("Press S to save.")
print("Press U to undo last slot.")
print("Press C to clear current unfinished points.")
print("Press Q to quit without saving.")

while True:
    cv2.imshow("Draw Parking Slots", display_frame)
    key = cv2.waitKey(1) & 0xFF

    if key == ord("s"):
        with open(SLOTS_FILE, "wb") as f:
            pickle.dump(slots, f)
        print(f"Saved {len(slots)} slots to {SLOTS_FILE}")
        break

    elif key == ord("u"):
        if slots:
            removed = slots.pop()
            print("Removed last slot:", removed)
            redraw()

    elif key == ord("c"):
        current_points.clear()
        print("Cleared current points")
        redraw()

    elif key == ord("q"):
        print("Quit without saving")
        break

cap.release()
cv2.destroyAllWindows()