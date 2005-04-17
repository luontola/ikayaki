xcopy /y /s /exclude:excluded.txt resources tmp\resources\
xcopy /y /s /exclude:excluded.txt classes\ikayaki tmp\ikayaki\
xcopy /y /s /exclude:excluded.txt classes\jutil tmp\jutil\
xcopy /y src\IKAYAKI.MF tmp\META-INF\MANIFEST.MF
copy /y lib\*.jar tmp\
copy /y lib\*.dll tmp\
copy /y lib\*.properties tmp\
pause