ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifeq ($(origin NETLOGO), undefined)
  NETLOGO=../..
endif

ifeq ($(origin SCALA_JAR), undefined)
  SCALA_JAR=$(NETLOGO)/lib/scala-library.jar
endif

ifeq ($(origin SCALA_HOME), undefined)
  SCALA_HOME=/usr/local
endif

SCALA_SRCS=$(wildcard src/*.scala)
JAVA_SRCS=$(wildcard src/*.java)

gogo.jar: $(SRCS) manifest.txt Makefile RXTXcomm.jar
	mkdir -p classes
	$(SCALA_HOME)/bin/scalac -deprecation -unchecked -encoding us-ascii -classpath $(NETLOGO)/NetLogo.jar -d classes $(SCALA_SRCS) $(JAVA_SRCS)
	$(JAVA_HOME)/bin/javac -g -deprecation -Xlint:all -Xlint:-serial -Xlint:-path -encoding us-ascii -source 1.5 -target 1.5 -classpath $(NETLOGO)/NetLogo.jar:$(SCALA_JAR):RXTXcomm.jar:classes -d classes $(JAVA_SRCS)
	jar cmf manifest.txt gogo.jar -C classes .

gogo.zip: gogo.jar
	rm -rf gogo
	mkdir gogo
	cp -rp gogo.jar README.md Makefile src manifest.txt gogo
	zip -rv gogo.zip gogo
	rm -rf gogo

RXTXcomm.jar:
	mkdir -p lib/Mac\ OS\ X lib/Windows lib/Linux-x86 lib/Linux-amd64
	curl -f -S 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/RXTXcomm.jar' -o RXTXcomm.jar
	curl -f -S 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/mac-10.5/librxtxSerial.jnilib' -o lib/Mac\ OS\ X/librxtxSerial.jnilib
	curl -f -S 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/win32/rxtxSerial.dll' -o lib/Windows/rxtxSerial.dll
	curl -f -S 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/i686-pc-linux-gnu/librxtxParallel.so' -o lib/Linux-x86/librxtxParallel.so
	curl -f -S 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/i686-pc-linux-gnu/librxtxSerial.so' -o lib/Linux-x86/librxtxSerial.so
	curl -f -S 'http://ccl.northwestern.edu/devel/rxtx-2.2pre2-bins/x86_64-unknown-linux-gnu/librxtxSerial.so' -o lib/Linux-amd64/librxtxSerial.so
