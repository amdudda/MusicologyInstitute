CREATE TABLE Instrument (
InstID int NOT NULL AUTO_INCREMENT, 
InstName Varchar(50), 
InstType Varchar(30), 
Subtype Varchar(30), 
AcquiredDate date, 
AcquiredFrom int, 
PurchasePrice Decimal(12,2), 
InsuranceValue Decimal(12,2), 
Location Varchar(10), 
Height double, 
Width double, 
Depth double, 
Region Varchar(100), 
Country Varchar(100), 
Culture Varchar(100), 
Tuning Varchar(20), 
LowNote Varchar(2), 
HighNote Varchar(2), 
Description varchar(1500), 
IsALoan boolean, 
PRIMARY KEY (InstID),
FOREIGN KEY (AcquiredFrom) REFERENCES Contact(ContactID),
FOREIGN KEY (Country) REFERENCES Countries(CountryName)
);