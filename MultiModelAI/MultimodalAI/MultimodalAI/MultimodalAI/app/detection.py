from ultralytics import YOLO
import torch

model = YOLO('yolov8n.pt')


def detect_objects(frame, class_names=None):

    results = model(frame)

    filtered_boxes = []
    for det in results[0].boxes.data.tolist():
        x1, y1, x2, y2, confidence, class_id = det
        if confidence > 0.60:
            filtered_boxes.append([x1, y1, x2, y2, confidence, class_id])

    if len(filtered_boxes) > 0:
        results[0].boxes.data = torch.tensor(filtered_boxes, device=results[0].boxes.data.device,
                                             dtype=results[0].boxes.data.dtype)
    else:
        results[0].boxes.data = torch.empty((0, 6), device=results[0].boxes.data.device,
                                            dtype=results[0].boxes.data.dtype)

    annotated_frame = results[0].plot()

    detections = []
    for x1, y1, x2, y2, confidence, class_id in filtered_boxes:
        class_name = model.names[int(class_id)]
        if class_names and class_name not in class_names:
            continue

        detections.append({
            "x1": int(x1),
            "y1": int(y1),
            "x2": int(x2),
            "y2": int(y2),
            "confidence": round(confidence, 2),
            "class_name": class_name
        })

    return annotated_frame, detections
