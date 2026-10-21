-- SQL Script to restart tables

-- -- Restart All Table 
-- TRUNCATE TABLE GymClassBooking;
-- TRUNCATE TABLE UserProfile;
-- TRUNCATE TABLE Customer;

-- TRUNCATE TABLE Membership;
-- TRUNCATE TABLE ProfileAddress;
TRUNCATE TABLE GymClass;
INSERT INTO GymClass (ClassName, ClassDescription, StartDateTime, EndDateTime, MaxCapacity, AvgCaloriesBurnedPerDay, TrainerId) VALUES
('Power Lifting', 'Focus on Compound movements.', '2026-06-01 09:00:00', '2026-06-01 10:30:00', 10, 450.0, 1),
('Beginner Yoga', 'Focus on flexibility.', '2026-06-01 11:00:00', '2026-06-01 12:00:00', 20, 250.0, 1),
('HIIT Blast', 'High intensity cardio.', '2026-06-02 08:00:00', '2026-06-02 09:00:00', 15, 600.0, 1),
('Scuba Dubai', 'Scoob your inner self.', '2026-06-04 08:00:00', '2026-06-04 09:00:00', 15, 267.0, 1),
('Muay Thai Striking', 'Learn basic kicks, elbows, and knees with fight-prep intensity.', '2026-06-01 17:00:00', '2026-06-01 18:30:00', 12, 720.0, 5),
('Spin & Sweat', 'High-energy rhythm indoor cycling to crush your morning cardio.', '2026-06-02 06:00:00', '2026-06-02 07:00:00', 25, 550.0, 4),
('Core Crusher', 'Targeted abs, lower back, and obliques burning session.', '2026-06-02 12:00:00', '2026-06-02 12:45:00', 20, 320.0, 4),
('Rugby Fit Intervals', 'Power endurance circuits inspired by professional rugby training.', '2026-06-03 07:00:00', '2026-06-03 08:30:00', 18, 780.0, 3),
('Zumba Dance Party', 'Groove your way into a heavy caloric deficit. Rhythm optional.', '2026-06-03 18:00:00', '2026-06-03 19:00:00', 30, 420.0, 2),
('Olympic Weightlifting', 'Technical breakdown of the snatch, clean, and jerk.', '2026-06-04 10:00:00', '2026-06-04 11:30:00', 8, 480.0, 2),
('Pilates Alignment', 'Low-impact muscle toning, flexibility, and posture control.', '2026-06-04 16:00:00', '2026-06-04 17:00:00', 15, 290.0, 4),
('MetCon Madness', 'Metabolic conditioning to completely test your VO2 max thresholds.', '2026-06-05 06:30:00', '2026-06-05 07:30:00', 15, 660.0, 1),
('Boxing Conditioning', 'Heavy bag drills, pad combinations, and intense footwork.', '2026-06-05 17:30:00', '2026-06-05 18:30:00', 14, 690.0, 3),
('Flexibility & Mobility', 'Deep active recovery stretching and joint restoration.', '2026-06-06 10:00:00', '2026-06-06 11:00:00', 25, 180.0, 2),
('Strongman Sunday', 'Flip commercial truck tires, carry logs, and build unyielding might.', '2026-06-07 09:00:00', '2026-06-07 11:00:00', 10, 880.0, 1);

-- Empty all gym class booking and reset identity to 1
TRUNCATE TABLE GymClassBooking;
UPDATE GymClass
SET CurrentCapacity = 0;