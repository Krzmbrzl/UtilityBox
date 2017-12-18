package raven.utilityBox.activator;

import java.io.File;

import raven.utilityBox.actions.Mp3FilenameFormatAction;

public class Activator {
	
	public static void main(String[] args) {
		Mp3FilenameFormatAction formatAction = new Mp3FilenameFormatAction(
				new File("/mint/" + System.getProperty("user.home") + "/Music/Gekauft/New"));
		
		formatAction.run();
		
	}
	
}
