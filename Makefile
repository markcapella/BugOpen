
# *****************************************************
# Variables to control Makefile operation

JCOMPILER = javac
JFLAGS = \
	--module-path /snap/openjfx/current/sdk/lib/ \
	--add-modules javafx.controls

JRUNTIME = java

# ****************************************************
# Targets needed to build the executable from the source folder

BugOpen: BugOpen.java
	@if [ ! -d "/snap/openjfx/current" ]; then \
		echo "Error! The openjfx package is not installed, but is required."; \
		echo "   try 'sudo snap install openjfx', then re-run this make."; \
		echo ""; \
		exit 1; \
	fi

	$(JCOMPILER) $(JFLAGS) BugOpen.java

	@echo "Build Done !"

# ****************************************************
# Target needed to run the executable from the source folder

run: BugOpen
	@if [ ! -d "/snap/openjfx/current" ]; then \
		echo "Error! The openjfx package is not installed, but is required."; \
		echo "   try 'sudo snap install openjfx', then re-run this make."; \
		echo ""; \
		exit 1; \
	fi

	$(JRUNTIME) $(JFLAGS) BugOpen

	@echo "Run Done !"

# ****************************************************
# Target needed to install the executable to user .local

install: BugOpen
	@if [ ! -f "BugOpen.class" ]; then \
	    echo "Executable not found !"; \
		echo "---> Did you run make yet?"; \
		exit 1; \
	fi

	# Kill any active instances.
	for FILE in $$(pgrep java) ; do \
		ps -p $$FILE -o args --no-headers | egrep BugOpen && kill $$FILE; \
	done

	rm -rf ~/.local/BugOpen
	mkdir ~/.local/BugOpen
	cp 'BugOpen.class' ~/.local/BugOpen
	cp 'BugOpen$$1.class' ~/.local/BugOpen
	cp 'BugOpen$$2.class' ~/.local/BugOpen

	cp 'BugOpen.png' ~/.local/BugOpen
	cp 'BugOpen.png' ~/.local/share/icons/hicolor/48x48/apps/

	cp 'BugOpen.desktop' ~/Desktop

	@echo "Install Done !"

# ****************************************************
# Target needed to uninstall the executable from user .local

uninstall:
	# Kill any active instances.
	for FILE in $$(pgrep java) ; do \
		ps -p $$FILE -o args --no-headers | egrep BugOpen && kill $$FILE; \
	done

	rm -rf ~/.local/BugOpen

	rm -f ~/.local/share/icons/hicolor/48x48/apps/BugOpen.png

	rm -f ~/Desktop/BugOpen.desktop

	@echo "Uninstall Done !"

# ****************************************************
# Target needed to clean the source folder for a fresh make

clean:
	rm -f 'BugOpen.class'
	rm -f 'BugOpen$$1.class'
	rm -f 'BugOpen$$2.class'

	@echo "Clean Done !"
