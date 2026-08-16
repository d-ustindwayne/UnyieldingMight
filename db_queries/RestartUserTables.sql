-- SQL Script to restart the userbase

-- 1. DROP TABLES (In dependency order)
-- We must drop GymClassBooking first because it references UserProfile
DROP TABLE GymClassBooking;
DROP TABLE UserProfile;
DROP TABLE Customer;

-- 2. RECREATE TABLES

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
    
    Age AS (DATEDIFF(YEAR, DateOfBirth, GETDATE()) - 
           CASE WHEN DATEADD(YEAR, DATEDIFF(YEAR, DateOfBirth, GETDATE()), DateOfBirth) > GETDATE() THEN 1 ELSE 0 END),
           
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    DateOfBirth DATE NOT NULL,
    Gender VARCHAR(10) NOT NULL,
    AddressId INT NULL,
    CustomerId INT NULL,
    UserClass VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',

    CONSTRAINT FK_UserProfile_Address FOREIGN KEY (AddressId) REFERENCES ProfileAddress (AddressId),
    CONSTRAINT FK_UserProfile_Customer FOREIGN KEY (CustomerId) REFERENCES Customer (CustomerId)
);

CREATE TABLE GymClassBooking (
    HistoryId INT IDENTITY(1, 1) PRIMARY KEY,
    ProfileId INT NOT NULL,
    ClassId INT NOT NULL,
    BookingStatus VARCHAR(20) DEFAULT 'ONGOING',
    BookingDate DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_History_User FOREIGN KEY (ProfileId) REFERENCES UserProfile (ProfileId),
    CONSTRAINT FK_History_Class FOREIGN KEY (ClassId) REFERENCES GymClass (ClassId)
);
GO
