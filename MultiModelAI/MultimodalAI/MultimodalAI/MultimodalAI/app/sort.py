import numpy as np
from pykalman import KalmanFilter


class KalmanBoxTracker:
    count = 0

    def __init__(self, bbox):

        self.kf = KalmanFilter(
            transition_matrices=np.array([
                [1, 0, 0, 0, 1, 0, 0],
                [0, 1, 0, 0, 0, 1, 0],
                [0, 0, 1, 0, 0, 0, 1],
                [0, 0, 0, 1, 0, 0, 0],
                [0, 0, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 0, 1, 0],
                [0, 0, 0, 0, 0, 0, 1],
            ]),
            observation_matrices=np.array([
                [1, 0, 0, 0, 0, 0, 0],
                [0, 1, 0, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 0, 0],
                [0, 0, 0, 1, 0, 0, 0],
            ]),
        )

        self.kf.initial_state_mean = np.zeros(7)
        self.kf.initial_state_covariance = np.eye(7) * 10.0

        self.time_since_update = 0
        self.id = KalmanBoxTracker.count
        KalmanBoxTracker.count += 1
        self.history = []
        self.hits = 0
        self.hit_streak = 0
        self.age = 0

        self.kf = self.kf.em([convert_bbox_to_z(bbox)], n_iter=5)

    def update(self, bbox):

        z = convert_bbox_to_z(bbox)
        self.kf = self.kf.em(z, n_iter=5)
        self.kf.initial_state_mean = z.flatten()

        self.time_since_update = 0
        self.history = []
        self.hits += 1
        self.hit_streak += 1

    def predict(self):

        self.age += 1
        if self.time_since_update > 0:
            self.hit_streak = 0
        self.time_since_update += 1

        predicted_state = self.kf.sample()[0].flatten()
        bbox = convert_x_to_bbox(predicted_state)
        self.history.append(bbox)
        return bbox

    def get_state(self):

        return self.history[-1] if self.history else None


def convert_bbox_to_z(bbox):

    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    x = bbox[0] + w / 2.0
    y = bbox[1] + h / 2.0
    s = w * h  # scale
    r = w / float(h)  # aspect ratio
    return np.array([x, y, s, r]).reshape((4, 1))


def convert_x_to_bbox(x, score=None):

    w = np.sqrt(x[2] * x[3])
    h = x[2] / w
    x1 = x[0] - w / 2.0
    y1 = x[1] - h / 2.0
    x2 = x[0] + w / 2.0
    y2 = x[1] + h / 2.0
    if score is None:
        return np.array([x1, y1, x2, y2]).reshape((1, 4))
    else:
        return np.array([x1, y1, x2, y2, score]).reshape((1, 5))


def iou_batch(bb_test, bb_gt):

    bb_gt = np.expand_dims(bb_gt, 0)
    bb_test = np.expand_dims(bb_test, 1)

    xx1 = np.maximum(bb_test[..., 0], bb_gt[..., 0])
    yy1 = np.maximum(bb_test[..., 1], bb_gt[..., 1])
    xx2 = np.minimum(bb_test[..., 2], bb_gt[..., 2])
    yy2 = np.minimum(bb_test[..., 3], bb_gt[..., 3])
    w = np.maximum(0., xx2 - xx1)
    h = np.maximum(0., yy2 - yy1)
    wh = w * h
    o = wh / (
        (bb_test[..., 2] - bb_test[..., 0]) * (bb_test[..., 3] - bb_test[..., 1]) +
        (bb_gt[..., 2] - bb_gt[..., 0]) * (bb_gt[..., 3] - bb_gt[..., 1]) - wh)
    return o


class Sort:

    def __init__(self, max_age=1, min_hits=3, iou_threshold=0.3):
        self.max_age = max_age
        self.min_hits = min_hits
        self.iou_threshold = iou_threshold
        self.trackers = []
        self.frame_count = 0

    def update(self, detections):

        self.frame_count += 1
        trackers = np.zeros((len(self.trackers), 5))
        for t, tracker in enumerate(self.trackers):
            pos = tracker.predict()[0]
            trackers[t, :4] = pos.flatten()

        matched, unmatched_dets, unmatched_trks = self.associate_detections_to_trackers(detections, trackers)

        for t, tracker in enumerate(self.trackers):
            if t not in unmatched_trks:
                d = detections[matched[t, 0]]
                tracker.update(d[:4])

        self.trackers = [t for t in self.trackers if t.time_since_update < self.max_age]
        for i in unmatched_dets:
            self.trackers.append(KalmanBoxTracker(detections[i]))

        return np.array([[*t.get_state()[0].flatten(), t.id] for t in self.trackers])

    def associate_detections_to_trackers(self, detections, trackers):

        if len(trackers) == 0:
            return np.empty((0, 2)), np.arange(len(detections)), []

        iou = iou_batch(detections, trackers)
        matched = np.argwhere(iou > self.iou_threshold)
        unmatched_dets = np.setdiff1d(np.arange(len(detections)), matched[:, 0])
        unmatched_trks = np.setdiff1d(np.arange(len(trackers)), matched[:, 1])

        return matched, unmatched_dets, unmatched_trks
