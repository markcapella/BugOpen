
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
ifneq ($(shell id -u), 0)
	@echo "You must be root to perform this action. Please re-run with:"
	@echo "   sudo make install"
	@echo
	@exit 1;
endif

	@echo
	@echo "sudo make install: starts ..."

	rm -rf /usr/local/BugOpen
	mkdir /usr/local/BugOpen

	cp *.class /usr/local/BugOpen

	cp 'BugOpen.png' /usr/local/BugOpen
	cp 'BugOpen.png' /usr/share/icons/hicolor/48x48/apps/

	cp 'BugOpen.desktop' /usr/share/applications/
	sudo -u $$SUDO_USER \
		cp '/usr/share/applications/BugOpen.desktop' /home/$$SUDO_USER/Desktop

	@echo
	@echo "Install Done !"
	@echo

# ****************************************************
# Target needed to uninstall the executable from user .local

uninstall:
ifneq ($(shell id -u), 0)
	@echo "You must be root to perform this action. Please re-run with:"
	@echo "   sudo make uninstall"
	@echo
	@exit 1;
endif

	@echo
	@echo "sudo make uninstall: starts ..."

	rm -rf /usr/local/BugOpen

	rm -f /usr/share/icons/hicolor/48x48/apps/BugOpen.png

	rm -f /usr/share/applications/BugOpen.desktop
	sudo -u $$SUDO_USER \
		rm -f /home/$$SUDO_USER/Desktop/BugOpen.desktop

	@echo
	@echo "Uninstall Done !"
	@echo

# ****************************************************
# Target needed to clean the source folder for a fresh make

clean:
	rm -f *.class

	@echo
	@echo "Clean Done !"
	@echo