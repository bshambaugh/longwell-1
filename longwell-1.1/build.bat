@echo off

rem ----- Ignore system CLASSPATH variable
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=
for %%i in (lib\endorsed\*.jar) do call tools\bin\appendcp.bat %%i

rem ----- Use Ant shipped. Ignore installed in the system Ant
set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools

set OLD_ANT_OPTS=%ANT_OPTS%
set ANT_OPTS=-Xms32M -Xmx512M -Djava.endorsed.dirs=lib\endorsed
rem set ADDITIONAL_ANT_OPTS=-listener net.sf.antcontrib.perf.AntPerformanceListener

call %ANT_HOME%\bin\ant -logger org.apache.tools.ant.DefaultLogger -emacs %ADDITIONAL_ANT_OPTS% %1 %2 %3 %4 %5 %6 %7 %8 %9

rem ----- Restore ANT_HOME and ANT_OPTS
set ANT_HOME=%OLD_ANT_HOME%
set OLD_ANT_HOME=
set ANT_OPTS=%OLD_ANT_OPTS%
set OLD_ANT_OPTS=

rem ----- Restore CLASSPATH
set CLASSPATH=%OLD_CLASSPATH%
set OLD_CLASSPATH=
