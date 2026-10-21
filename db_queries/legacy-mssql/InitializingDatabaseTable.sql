-- SQL Script to initialize database

-- 1. DROP EXISTING TABLES (Dependency order)
DROP TABLE GymClassBooking;
DROP TABLE UserProfile;
DROP TABLE Customer;
DROP TABLE GymClass;
DROP TABLE Trainer;
DROP TABLE Membership;
DROP TABLE ProfileAddress;

-- 2. CREATE TABLES
CREATE TABLE ProfileAddress (
    AddressId INT IDENTITY(1, 1) PRIMARY KEY,
    Street VARCHAR(100) NOT NULL,
    City VARCHAR(50) NOT NULL,
    Region VARCHAR(50) NOT NULL,
    Postcode VARCHAR(6) NOT NULL,
    Country VARCHAR(50) NOT NULL
);

CREATE TABLE Membership (
    MembershipId INT IDENTITY(1000, 1) PRIMARY KEY,
    Email VARCHAR(100) NOT NULL UNIQUE,
    DateOfBirth DATE NOT NULL,
    Gender VARCHAR(6) NOT NULL,
    Height FLOAT NOT NULL,
    Weight FLOAT NOT NULL,
    ActivityMultiplier FLOAT NOT NULL,
    TDEE FLOAT NOT NULL,
    AddressId INT NULL,
    CONSTRAINT FK_Membership_Address FOREIGN KEY (AddressId) REFERENCES ProfileAddress (AddressId)
);

CREATE TABLE Customer (
    CustomerId INT IDENTITY(1, 1) PRIMARY KEY,
    MembershipId INT DEFAULT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Height FLOAT NOT NULL,
    Weight FLOAT NOT NULL,
    ActivityMultiplier FLOAT NOT NULL,
    TDEE FLOAT NOT NULL,
    CONSTRAINT FK_Customer_Membership FOREIGN KEY (MembershipId) REFERENCES Membership (MembershipId)
);

CREATE TABLE UserProfile (
    ProfileId INT IDENTITY(1, 1) PRIMARY KEY,
    Email VARCHAR(100) NOT NULL UNIQUE,
    ProfilePassword VARCHAR(64) NOT NULL, -- SHA-256 encryption
    
    Age AS (DATEDIFF(YEAR, DateOfBirth, GETDATE()) - CASE WHEN
    DATEADD(YEAR, DATEDIFF(YEAR, DateOfBirth, GETDATE()), DateOfBirth) > GETDATE() THEN 1 ELSE 0 END),
           
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    DateOfBirth DATE NOT NULL,
    Gender VARCHAR(6) NOT NULL,
    AddressId INT NULL,
    CustomerId INT NULL,
    UserClass VARCHAR(8) NOT NULL DEFAULT 'CUSTOMER',

    CONSTRAINT FK_UserProfile_Address FOREIGN KEY (AddressId) REFERENCES ProfileAddress (AddressId),
    CONSTRAINT FK_UserProfile_Customer FOREIGN KEY (CustomerId) REFERENCES Customer (CustomerId)
);

CREATE TABLE Trainer (
    TrainerId INT IDENTITY(1, 1) PRIMARY KEY,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    ProfileDescription VARCHAR(MAX) NOT NULL
);

CREATE TABLE GymClass (
    ClassId INT IDENTITY(1, 1) PRIMARY KEY,
    ClassName VARCHAR(100) NOT NULL,
    ClassDescription NVARCHAR(MAX) NULL,
    StartDateTime DATETIME NOT NULL,
    EndDateTime DATETIME NOT NULL,
    MaxCapacity INT NOT NULL,
    CurrentCapacity INT DEFAULT 0,
    ClassStatus VARCHAR(10) DEFAULT 'ONGOING',
    AvgCaloriesBurnedPerDay FLOAT NOT NULL,
    TrainerId INT NULL,
    CONSTRAINT FK_GymClass_Trainer FOREIGN KEY (TrainerId) REFERENCES Trainer (TrainerId)
);

CREATE TABLE GymClassBooking (
    HistoryId INT IDENTITY(1, 1) PRIMARY KEY,
    ProfileId INT NOT NULL,
    ClassId INT NOT NULL,
    BookingStatus VARCHAR(10) DEFAULT 'ONGOING',
    BookingDate DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_History_User FOREIGN KEY (ProfileId) REFERENCES UserProfile (ProfileId),
    CONSTRAINT FK_History_Class FOREIGN KEY (ClassId) REFERENCES GymClass (ClassId)
);

-- 3. POPULATE TEST DATA

INSERT INTO ProfileAddress (Street, City, Region, Postcode, Country) VALUES
('789 Admin Way', 'Papatoetoe', 'Auckland', '2345', 'New Zealand'),
('100 West Street', 'Pukekohe', 'Auckland', '2120', 'New Zealand'),
('67 East Caloocan', 'Macintosh', 'Ratatuwi', '1121', 'New Zealand');

INSERT INTO UserProfile (Email, ProfilePassword, FirstName, LastName, DateOfBirth, Gender, AddressId, CustomerId, UserClass) VALUES
('grum7@manukaumail.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Justin', 'Grumal', '2001-11-07', 'MALE', 1, NULL, 'ADMIN'),
('loisdds30@gmail.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Lois Dara', 'Sanglap', '2002-03-05', 'FEMALE', 1, NULL, 'ADMIN'),
('nepomucenodwayne@gmail.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Dustin Dwayne', 'Diaz', '2005-04-14', 'MALE', 1, NULL, 'ADMIN');

INSERT INTO Trainer (FirstName, LastName, ProfileDescription) VALUES
('Dwayne', 'Johnson', 'Pro bodybuilder and MMA enthusiast.'),
('Dara', 'Sanglap', 'Pro bodybuilder and MMA enthusiast.'),
('Sonny Bill', 'Williams', 'All Black legend and heavyweight boxing champ. High intensity only.'),
('Chloe', 'Ting', 'Core strength wizard and bodyweight transformation expert.'),
('Israel', 'Adesanya', 'Agility, kickboxing maestro, and stylebender of Auckland Central.');

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

INSERT INTO Membership (Email, DateOfBirth, Gender, Height, Weight, ActivityMultiplier, TDEE, AddressId) VALUES
('dustindiaz101010@gmail.com', '2005-04-14', 'MALE', 163.45, 56.71, 1.2, 1787.2, 3),
('jasminesisontaboy015@gmail.com', '2005-04-15', 'FEMALE', 145.45, 47.13, 1.2, 1723.28, 2),
('justindegrumal28@outlook.com', '2005-11-28', 'MALE', 165.45, 67.13, 1.55, 2123.22, 1),
('ahmad.wedyan@manukau.ac.nz', '1984-07-12', 'MALE', 172.72, 69.14, 1.55, 2134.16, 2),
('alaa.aljanabi@manukau.ac.nz', '1967-01-03', 'MALE', 171.72, 62.12, 1.275, 1981.92, 4);