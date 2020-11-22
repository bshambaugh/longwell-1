@echo off
::
:: Configuration variables
::
:: JAVA_HOME
::   Home of Java installation.

:: ----- Verify and Set Required Environment Variables -------------------------

if not "%JAVA_HOME%" = "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto end

:gotJavaHome
if not "%LONGWELL_TOOLS%" = "" goto gotLongwellTools
set LONGWELL_TOOLS=build\longwell-tools.jar

:gotLongwellTools
if exist %LONGWELL_TOOLS% goto toolsJarExists
echo "You must build the tool .jar's first by running ./build.sh tools"
goto end

:toolsJarExists
if defined %1 goto doAction
echo "Usage: $0 (action) (arguments)"
echo "actions:"
echo "  LoadDb    Load RDF data into a database"
goto end

:doAction
:: ----- Local variables
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=
for %%i in (lib\*.jar) do call tools\bin\appendcp.bat %%i
for %%i in (lib\endorsed\*.jar) do call tools\bin\appendcp.bat %%i
call tools\bin\appendcp.bat %LONGWELL_TOOLS%
for %%i in (build\*.jar) do call tools\bin\appendcp.bat %%i
set JAVA="%JAVA_HOME%\bin\java.exe"
set JAVA_OPTS="-Xmx512M"

call %JAVA% %JAVA_OPTS% -cp %CP% edu.mit.simile.tools.%1 %2 %3 %4 %5 %6 %7 %8 %9

:end
set CLASSPATH=%OLD_CLASSPATH%
set OLD_CLASSPATH=
set JAVA=
set JAVA_OPTS=
