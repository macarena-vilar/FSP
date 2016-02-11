/*
Created: 12/04/2015
Modified: 24/06/2015
Project: GANEMAX FSP
Model: GANEMAX FSP
Company: Menhir Sistemas SRL
Author: Leonardo A. Blumencweig
Version: 1.0
Database: DB2 v.10.5 (LUW)
*/

-- Create tables section -------------------------------------------------

-- Table FSP_INP_HEADER

CREATE TABLE FSP_INP_HEADER(
  gameID Char(1) NOT NULL,
  drawNR Bigint NOT NULL,
  drawDate Date NOT NULL
)
;

-- Table and Columns comments section
  
COMMENT ON COLUMN FSP_INP_HEADER.gameID IS 'One character gameID: ''E'' -> ELEGI2 , ''P'' -> Premio'
;
COMMENT ON COLUMN FSP_INP_HEADER.drawNR IS 'Sequential game Nr.'
;
COMMENT ON COLUMN FSP_INP_HEADER.drawDate IS 'Date of the game'
;

-- Add keys for table FSP_INP_HEADER

ALTER TABLE FSP_INP_HEADER ADD CONSTRAINT Key1 PRIMARY KEY (gameID,drawNR)
;

-- Table FSP_INP_TOTALS

CREATE TABLE FSP_INP_TOTALS(
  gameID Char(1) NOT NULL,
  drawNR Bigint NOT NULL,
  vendorID Char(1) NOT NULL,
  salesTotalQ Bigint NOT NULL,
  salesTotalAmount Decimal(20,2) NOT NULL,
  canceledTotalQ Bigint NOT NULL,
  canceledTotalAmount Decimal(20,2) NOT NULL
)
;

-- Table and Columns comments section
  
COMMENT ON COLUMN FSP_INP_TOTALS.vendorID IS 'One character vendor ID: ''G'' -> Gtech , ''D'' -> DTP'
;
COMMENT ON COLUMN FSP_INP_TOTALS.salesTotalQ IS 'Total quantity of non canceled sold tickets'
;
COMMENT ON COLUMN FSP_INP_TOTALS.salesTotalAmount IS 'Total amount of non canceled sold tickets'
;
COMMENT ON COLUMN FSP_INP_TOTALS.canceledTotalQ IS 'Total quantity of canceled tickets'
;
COMMENT ON COLUMN FSP_INP_TOTALS.canceledTotalAmount IS 'Total amountof canceled tickets'
;

-- Add keys for table FSP_INP_TOTALS

ALTER TABLE FSP_INP_TOTALS ADD CONSTRAINT Key2 PRIMARY KEY (gameID,drawNR,vendorID)
;

-- Table FSP_INP_DIVISIONS

CREATE TABLE FSP_INP_DIVISIONS(
  gameID Char(1) NOT NULL,
  drawNR Bigint NOT NULL,
  vendorID Char(1) NOT NULL,
  divNR Integer NOT NULL,
  winnersQ Bigint NOT NULL,
  winnersAmount Decimal(20,2) NOT NULL
)
;

-- Table and Columns comments section
  
COMMENT ON COLUMN FSP_INP_DIVISIONS.divNR IS 'Div number (1 ... n)'
;
COMMENT ON COLUMN FSP_INP_DIVISIONS.winnersQ IS 'Quantity of winners division "n"'
;
COMMENT ON COLUMN FSP_INP_DIVISIONS.winnersAmount IS 'Amount to pay to winners of division "n"'
;

-- Add keys for table FSP_INP_DIVISIONS

ALTER TABLE FSP_INP_DIVISIONS ADD CONSTRAINT Key3 PRIMARY KEY (gameID,drawNR,vendorID,divNR)
;

-- Table FSP_OUT_DATA

CREATE TABLE FSP_OUT_DATA(
  gameID Char(1) NOT NULL,
  drawNR Bigint NOT NULL,
  drawDate Date NOT NULL,
  salesTotalQ Bigint NOT NULL,
  salesTotalAmount Decimal(20,2) NOT NULL,
  canceledTotalQ Bigint NOT NULL,
  canceledTotalAmount Decimal(20,2) NOT NULL,
  totalAccum Decimal(20,2) NOT NULL DEFAULT 0
)
;

-- Table and Columns comments section
  
COMMENT ON COLUMN FSP_OUT_DATA.gameID IS 'One character gameID: ''E'' -> ELEGI2 , ''P'' -> Premio'
;
COMMENT ON COLUMN FSP_OUT_DATA.drawNR IS 'Sequential game Nr.'
;
COMMENT ON COLUMN FSP_OUT_DATA.drawDate IS 'Date of the game'
;
COMMENT ON COLUMN FSP_OUT_DATA.salesTotalQ IS 'Total quantity of non canceled sold tickets'
;
COMMENT ON COLUMN FSP_OUT_DATA.salesTotalAmount IS 'Total amount of non canceled sold tickets'
;
COMMENT ON COLUMN FSP_OUT_DATA.canceledTotalQ IS 'Total quantity of canceled tickets'
;
COMMENT ON COLUMN FSP_OUT_DATA.canceledTotalAmount IS 'Total amount of canceled tickets'
;
COMMENT ON COLUMN FSP_OUT_DATA.totalAccum IS 'Accumulated Jackpot'
;

-- Add keys for table FSP_INP_DIVISIONS

ALTER TABLE FSP_OUT_DATA ADD CONSTRAINT fsp_out_data_pk PRIMARY KEY (gameID,drawNR)
;
-- Table FSP_OUT_DIVISIONS

CREATE TABLE FSP_OUT_DIVISIONS(
  divNR Integer NOT NULL,
  winnersQ Bigint NOT NULL,
  winnersAmount Decimal(20,2) NOT NULL,
  gameID Char(1) NOT NULL,
  drawNR Integer NOT NULL
)
;

-- Table and Columns comments section
  
COMMENT ON COLUMN FSP_OUT_DIVISIONS.divNR IS 'Div number (1..n)'
;
COMMENT ON COLUMN FSP_OUT_DIVISIONS.winnersQ IS 'Quantity of winners division "n"'
;
COMMENT ON COLUMN FSP_OUT_DIVISIONS.winnersAmount IS 'Amount to pay to winners of division "n"'
;

-- Add keys for table FSP_OUT_DIVISIONS

ALTER TABLE FSP_OUT_DIVISIONS ADD CONSTRAINT Key3 PRIMARY KEY (divNR,gameID,drawNR)
;

-- Create relationships section ------------------------------------------------- 

ALTER TABLE FSP_INP_TOTALS ADD FOREIGN KEY (gameID, drawNR) REFERENCES FSP_INP_HEADER (gameID, drawNR)
;

ALTER TABLE FSP_INP_DIVISIONS ADD FOREIGN KEY (gameID, drawNR, vendorID) REFERENCES FSP_INP_TOTALS (gameID, drawNR, vendorID)
;

ALTER TABLE FSP_OUT_DIVISIONS ADD FOREIGN KEY (gameID, drawNR) REFERENCES FSP_OUT_DATA (gameID, drawNR)
;


