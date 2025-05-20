# Python AI + duration + accuracy hesaplama


import cv2
from ultralytics import YOLO
import numpy as np
from gpiozero import AngularServo
import time
import ast
import sys

def set_angleHorizontal(angle):
    servo1 = AngularServo(14, min_angle=0, max_angle=180, min_pulse_width=0.5/1000, max_pulse_width=2.5/1000)
    servo1.angle = angle
    time.sleep(0.5)
    servo1.close()

def set_angleVertical(angle):
    servo2 = AngularServo(15, min_angle=0, max_angle=180, min_pulse_width=0.5/1000, max_pulse_width=2.5/1000)
    servo2.angle = angle
    time.sleep(0.5)
    servo2.close()

def AI_foot_detection(verticalArray, horizontalArray):
    model = YOLO("yolov8n.pt")
    stepCounter = 0
    correctDetections = 0
    foot_detected = False
    AREA_WIDTH = 300
    AREA_HEIGHT = 200

    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Camera error")
        exit()

    ret, frame = cap.read()
    if not ret:
        print("Frame capture error")
        cap.release()
        exit()

    frame_height, frame_width = frame.shape[:2]
    center_x, center_y = frame_width // 2, frame_height // 2
    top_left_x = center_x - AREA_WIDTH // 2
    top_left_y = center_y - AREA_HEIGHT // 2
    bottom_right_x = center_x + AREA_WIDTH // 2
    bottom_right_y = center_y + AREA_HEIGHT // 2

    start_time = time.time()

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        mask = np.zeros_like(frame)
        mask[top_left_y:bottom_right_y, top_left_x:bottom_right_x] = frame[top_left_y:bottom_right_y, top_left_x:bottom_right_x]
        frame = mask

        results = model(frame, classes=[0], conf=0.5)

        for r in results:
            boxes = r.boxes.xyxy.cpu().numpy()
            for box in boxes:
                x1, y1, x2, y2 = map(int, box)
                foot_x = (x1 + x2) // 2
                foot_y = y2

                if (top_left_x <= foot_x <= bottom_right_x and
                    top_left_y <= foot_y <= bottom_right_y):
                    cv2.circle(frame, (foot_x, foot_y), 5, (0, 255, 0), -1)
                    cv2.putText(frame, f"Foot: ({foot_x}, {foot_y})", (10, 30),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)
                    foot_detected = True
                    correctDetections += 1

        if foot_detected:
            foot_detected = False
            set_angleVertical(verticalArray[stepCounter])
            set_angleHorizontal(horizontalArray[stepCounter])
            stepCounter += 1
            if stepCounter == len(verticalArray):
                end_time = time.time()
                duration = end_time - start_time
                accuracy = (correctDetections / len(verticalArray)) * 100
                print("Sequence complete")
                print(f"DURATION:{int(duration)}")
                print(f"ACCURACY:{accuracy:.2f}")
                cap.release()
                cv2.destroyAllWindows()
                return

        cv2.rectangle(frame, (top_left_x, top_left_y), (bottom_right_x, bottom_right_y), (0, 0, 255), 2)
        cv2.imshow('YOLO Foot Detection Area', frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python foot_detection.py <filename>")
        sys.exit(1)

    fileAddress = sys.argv[1]
    with open(fileAddress, "r") as file:
        line1 = file.readline().strip()
        line2 = file.readline().strip()

    vertArray = ast.literal_eval(line1)
    horizArray = ast.literal_eval(line2)
    AI_foot_detection(vertArray, horizArray)
