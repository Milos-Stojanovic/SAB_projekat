-- Kreiranje tabele

DROP DATABASE IF EXISTS KurirskaSluzba
go

CREATE DATABASE KurirskaSluzba
go

USE KurirskaSluzba
go

DROP TABLE IF EXISTS [Kupac]
go

DROP TABLE IF EXISTS [ZahtevZaKurira]
go

DROP TABLE IF EXISTS [Administrator]
go

DROP TABLE IF EXISTS [Vozi]
go

DROP TABLE IF EXISTS [Vozio]
go

DROP TABLE IF EXISTS [Vozilo]
go

DROP TABLE IF EXISTS [LokacijaMagazina]
go

DROP TABLE IF EXISTS [Kurir]
go

DROP TABLE IF EXISTS [Ponuda]
go

DROP TABLE IF EXISTS [ZahtevPaket]
go

DROP TABLE IF EXISTS [Korisnik]
go

DROP TABLE IF EXISTS [Adresa]
go

DROP TABLE IF EXISTS [Isporuka]
go

DROP TABLE IF EXISTS [Grad]
go

DROP TABLE IF EXISTS [Paket]
go

CREATE TABLE [Administrator]
( 
	[IdKor]              integer  NOT NULL 
)
go

CREATE TABLE [Adresa]
( 
	[IdAdr]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Ulica]              varchar(100)  NOT NULL ,
	[Broj]               integer  NOT NULL ,
	[IdGra]              integer  NOT NULL ,
	[x_koord]            integer  NOT NULL ,
	[y_koord]            integer  NOT NULL 
)
go

CREATE TABLE [Grad]
( 
	[IdGra]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[PostanskiBroj]      varchar(100)  NOT NULL ,
	[Naziv]              varchar(100)  NOT NULL 
)
go

CREATE TABLE [Korisnik]
( 
	[IdKor]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Ime]                varchar(100)  NOT NULL ,
	[Prezime]            varchar(100)  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Sifra]              varchar(100)  NOT NULL ,
	[IdAdr]              integer  NOT NULL 
)
go

CREATE TABLE [Kupac]
( 
	[IdKor]              integer  NOT NULL 
)
go

CREATE TABLE [Kurir]
( 
	[IdKor]              integer  NOT NULL ,
	[BrIsporucenihPaketa] integer  NOT NULL ,
	[Status]             integer  NOT NULL ,
	[OstvarenProfit]     decimal(10,3)  NOT NULL ,
	[VozackaDozvola]     varchar(9)  NOT NULL 
)
go

CREATE TABLE [LokacijaMagazina]
( 
	[IdLok]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdAdr]              integer  NOT NULL 
)
go

CREATE TABLE [Parkirano]
( 
	[IdVoz]              integer  NOT NULL ,
	[IdLok]              integer  NULL 
)
go

CREATE TABLE [Ponuda]
( 
	[IdPon]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[CenaIsporuke]       decimal(10,3)  NOT NULL ,
	[VremePrihvatanjaPonude] datetime  NULL ,
	[IdPak]              integer  NOT NULL ,
	[Lokacija]           integer  NOT NULL ,
	[StatusIsporuke]     char(18)  NULL 
)
go

CREATE TABLE [uPrevozu]
( 
	[IdPre]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Flag]               integer  NOT NULL ,
	[IdPak]              integer  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[IdVoz]              integer  NOT NULL 
)
go

CREATE TABLE [Vozi]
( 
	[IdKor]              integer  NOT NULL ,
	[IdVoz]              integer  NOT NULL ,
	[PredjenPut]         decimal(10,3)  NOT NULL ,
	[TrenutnaLokacija]   integer  NOT NULL 
)
go

CREATE TABLE [Vozilo]
( 
	[IdVoz]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[TipGoriva]          integer  NOT NULL ,
	[Potrosnja]          decimal(10,3)  NOT NULL ,
	[Nosivost]           decimal(10,3)  NOT NULL ,
	[RegBr]              varchar(10)  NOT NULL 
)
go

CREATE TABLE [Vozio]
( 
	[IdVozio]            integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[IdVoz]              integer  NOT NULL 
)
go

CREATE TABLE [ZahtevPaket]
( 
	[IdKor]              integer  NOT NULL ,
	[Tip]                integer  NOT NULL ,
	[Tezina]             integer  NOT NULL ,
	[PocetnaAdresa]      integer  NOT NULL ,
	[ZavrsnaAdresa]      integer  NOT NULL ,
	[VremeKreiranjaZahteva] datetime  NOT NULL ,
	[IdPak]              integer  IDENTITY ( 1,1 )  NOT NULL 
)
go

CREATE TABLE [ZahtevZaKurira]
( 
	[Podnosilac]         integer  NOT NULL ,
	[BrojVozacke]        varchar(9)  NOT NULL 
)
go

ALTER TABLE [Administrator]
	ADD CONSTRAINT [XPKAdministrator] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Adresa]
	ADD CONSTRAINT [XPKAdresa] PRIMARY KEY  CLUSTERED ([IdAdr] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([IdGra] ASC)
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XPKKorisnik] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Kupac]
	ADD CONSTRAINT [XPKKupac] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XPKKurir] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [LokacijaMagazina]
	ADD CONSTRAINT [XPKLokacijaMagazina] PRIMARY KEY  CLUSTERED ([IdLok] ASC)
go

ALTER TABLE [Parkirano]
	ADD CONSTRAINT [XPKParkirano] PRIMARY KEY  CLUSTERED ([IdVoz] ASC)
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [XPKPonuda] PRIMARY KEY  CLUSTERED ([IdPon] ASC)
go

ALTER TABLE [uPrevozu]
	ADD CONSTRAINT [XPKuPrevozu] PRIMARY KEY  CLUSTERED ([IdPre] ASC,[Flag] ASC)
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [XPKVozi] PRIMARY KEY  CLUSTERED ([IdKor] ASC,[IdVoz] ASC)
go

ALTER TABLE [Vozilo]
	ADD CONSTRAINT [XPKVozilo] PRIMARY KEY  CLUSTERED ([IdVoz] ASC)
go

ALTER TABLE [Vozio]
	ADD CONSTRAINT [XPKVozio] PRIMARY KEY  CLUSTERED ([IdVozio] ASC)
go

ALTER TABLE [ZahtevPaket]
	ADD CONSTRAINT [XPKZahtevPaket] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go

ALTER TABLE [ZahtevZaKurira]
	ADD CONSTRAINT [XPKZahtevZaKurira] PRIMARY KEY  CLUSTERED ([Podnosilac] ASC)
go


ALTER TABLE [Administrator]
	ADD CONSTRAINT [R_7] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Adresa]
	ADD CONSTRAINT [R_1] FOREIGN KEY ([IdGra]) REFERENCES [Grad]([IdGra])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Korisnik]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([IdAdr]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Kupac]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Kurir]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [LokacijaMagazina]
	ADD CONSTRAINT [R_2] FOREIGN KEY ([IdAdr]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Parkirano]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([IdVoz]) REFERENCES [Vozilo]([IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Parkirano]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([IdLok]) REFERENCES [LokacijaMagazina]([IdLok])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_17] FOREIGN KEY ([IdPak]) REFERENCES [ZahtevPaket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_27] FOREIGN KEY ([Lokacija]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [uPrevozu]
	ADD CONSTRAINT [R_30] FOREIGN KEY ([IdPak]) REFERENCES [ZahtevPaket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [uPrevozu]
	ADD CONSTRAINT [R_31] FOREIGN KEY ([IdKor],[IdVoz]) REFERENCES [Vozi]([IdKor],[IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([IdKor]) REFERENCES [Kurir]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([IdVoz]) REFERENCES [Vozilo]([IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_28] FOREIGN KEY ([TrenutnaLokacija]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Vozio]
	ADD CONSTRAINT [R_12] FOREIGN KEY ([IdKor]) REFERENCES [Kurir]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozio]
	ADD CONSTRAINT [R_13] FOREIGN KEY ([IdVoz]) REFERENCES [Vozilo]([IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [ZahtevPaket]
	ADD CONSTRAINT [R_15] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [ZahtevPaket]
	ADD CONSTRAINT [R_20] FOREIGN KEY ([PocetnaAdresa]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [ZahtevPaket]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([ZavrsnaAdresa]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [ZahtevZaKurira]
	ADD CONSTRAINT [R_8] FOREIGN KEY ([Podnosilac]) REFERENCES [Korisnik]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

--

-- Ogranicenje da u svakom trenutku jedno vozilo moze da vozi samo 1 kurir
-- i da kurir ne moze da vozi vise vozila
USE KurirskaSluzba
GO
ALTER TABLE [Vozi]
	ADD CONSTRAINT [special_constraint_1] UNIQUE ([IdKor])
GO
ALTER TABLE [Vozi]
	ADD CONSTRAINT [special_constraint_2] UNIQUE ([IdVoz])

--

-- Trigger za kreiranje ponude

USE [KurirskaSluzba]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER [dbo].[IsporukaTriger]
   ON  [dbo].[ZahtevPaket]
   for Insert,Update
AS 
BEGIN
	declare @kursor cursor

	declare @IdPak integer, @IdAdrOd integer , @IdAdrDo integer, @TipPaketa integer, @Tezina decimal(10,3)
	declare @OsnovnaCena decimal(10,3), @CenaPoKg decimal(10,3), @CenaIsporuke decimal(10,3)
	declare @EuklidskaDistanca decimal(10,3)

	declare @X_koord_od integer, @Y_koord_od integer
	declare @X_koord_do integer, @Y_koord_do integer


	set @kursor= cursor for
	select IdPak, PocetnaAdresa, ZavrsnaAdresa, Tip, Tezina
	from inserted

	open @kursor

	fetch from @kursor
	into
	@IdPak, @IdAdrOd, @IdAdrDo, @TipPaketa, @Tezina
	 

	while @@FETCH_STATUS=0
	begin

		if(@TipPaketa=0) --mali paket
		begin
			set @OsnovnaCena=115
			set @CenaPoKg=0
		end

		if(@TipPaketa=1) --standardni paket
		begin
			set @OsnovnaCena=175
			set @CenaPoKg=100
		end

		if(@TipPaketa=2) --nestandardni paket
		begin
			set @OsnovnaCena=250
			set @CenaPoKg=100
		end

		if(@TipPaketa=3) --lomljiv paket
		begin
			set @OsnovnaCena=350
			set @CenaPoKg=500
		end

		select @X_koord_od = X_koord, @Y_koord_od = Y_koord from Adresa
		where IdAdr = @IdAdrOd

		select @X_koord_do=X_koord, @Y_koord_do=Y_koord from Adresa
		where IdAdr=@IdAdrDo

		set @EuklidskaDistanca=SQRT(POWER(@X_koord_od-@X_koord_do,2) + POWER(@Y_koord_od-@Y_koord_do,2))
		set @CenaIsporuke=(@OsnovnaCena + @Tezina*@CenaPoKg) * @EuklidskaDistanca

		if exists(select IdPon from Ponuda where IdPak=@IdPak)
		begin
			update Ponuda
			set CenaIsporuke=@CenaIsporuke
			where IdPak=@IdPak
		end
		else begin
			insert into Ponuda (IdPak, StatusIsporuke, CenaIsporuke, VremePrihvatanjaPonude, Lokacija) values(@IdPak, 0, @CenaIsporuke, GETDATE(), @IdAdrOd)
		end


		fetch from @kursor
		into
		@IdPak, @IdAdrOd, @IdAdrDo, @TipPaketa, @Tezina

	end

	close @kursor
	deallocate @kursor


END
