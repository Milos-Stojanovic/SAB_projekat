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
