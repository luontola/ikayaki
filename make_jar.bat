xcopy /y /s /exclude:excluded.txt resources tmp\resources\
xcopy /y /s /exclude:excluded.txt classes\ikayaki tmp\ikayaki\
xcopy /y /s /exclude:excluded.txt classes\jutil tmp\jutil\
xcopy /y src\IKAYAKI.MF tmp\META-INF\MANIFEST.MF
mkdir tmp\lib
copy /y lib\*.jar tmp\lib\
copy /y lib\*.dll tmp\lib\
copy /y lib\*.properties tmp\lib\
pause