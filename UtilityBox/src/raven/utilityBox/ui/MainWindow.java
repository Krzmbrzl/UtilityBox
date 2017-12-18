package raven.utilityBox.ui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MainWindow {
	
	public void open() {
		initializeParts();
	}
	
	protected void initializeParts() {
		// TODO: Create Display in new UI thread
		Shell shell = new Shell(Display.getDefault());
		shell.setSize(800, 400);
		
		shell.setLayout(new FillLayout());
		
		shell.open();
		
		
	}
}
