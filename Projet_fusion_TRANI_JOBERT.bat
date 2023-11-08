cd OneDollarIvy
start OneDollarIvy.exe
cd ..

REM Lancer Palette

cd Palette/
start Palette.exe
cd ../

cd sra5
sra5.exe -b 127.255.255.255:2010 -g grammaire_parole.grxml -p on
pause
