package org.jcryptool.crypto.analysis.substitution.ui.modules;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.jcryptool.core.operations.algorithm.classic.textmodify.TransformData;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.ControlHatcher;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.TextTransformationDisplayer;
import org.jcryptool.crypto.analysis.substitution.ui.wizard.loadtext.LoadTextWizard;
import org.jcryptool.crypto.ui.textsource.TextInputWithSource;
import org.jcryptool.crypto.ui.textsource.TextInputWithSourceDisplayer;

public class TextLoadController extends Composite {

	private TextInputWithSourceDisplayer displayer;
	private Link loadTextLink;
	private List<Observer> observers;
	private Composite layoutRoot;
	private TextTransformationDisplayer transformDisplay;
	private TextInputWithSource text;
	private TransformData transformData;
	private Composite compDisplayer;
	private Button btnLoadText;
	private boolean showFrontLabel;
	private boolean oneRow;
	
	private ControlHatcher beforeWizardTextParasiteLabel = null;
	private ControlHatcher afterWizardTextParasiteLabel = null;

	public TextInputWithSource getText() {
		return text;
	}
	
	public TransformData getTransformData() {
		return transformData;
	}
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public TextLoadController(Composite parent, Composite layoutRoot, int style, boolean showFrontLabel, boolean oneRow) {
		super(parent, style);
		this.layoutRoot = layoutRoot;
		this.showFrontLabel = showFrontLabel;
		this.oneRow = oneRow;
		this.observers = new LinkedList<Observer>(); 
		
		this.text = null;
		this.transformData = new TransformData();
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 1;
		setLayout(layout);
		createButtonLoadCtrls();
		createDisplayerCtrls();
		
		setTextData(null, null, false);
	}

	private Button createButtonLoadCtrls() {
		btnLoadText = new Button(this, SWT.NONE);
		btnLoadText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnLoadText.setText("Load text...");
		
		btnLoadText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadTextWithBtnClick();
			}
		});
		
		return btnLoadText;
	}
	
	private void createDisplayerCtrls() {
		compDisplayer = new Composite(this, SWT.NONE);
		compDisplayer.setLayout(new GridLayout(3-(showFrontLabel?0:1)+(oneRow?1:0), false));
		compDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if(showFrontLabel) {
			Label lblLoadedText = new Label(compDisplayer, SWT.NONE);
			lblLoadedText.setText("Loaded text:");
		}
		
		displayer = new TextInputWithSourceDisplayer(compDisplayer, this, new TextInputWithSource(""), new TextInputWithSourceDisplayer.Style(true, false));
		displayer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		
		createTransformDisplays(compDisplayer);
		
		if(showFrontLabel && !oneRow) {
			new Label(compDisplayer, SWT.NONE);
		}
		
		loadTextLink = new Link(compDisplayer, SWT.NONE);
		loadTextLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, oneRow?1:2, 1));
		loadTextLink.setText("<A>choose another text...</A>");
		
		loadTextLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadTextWithLinkClick();
			}
		});
	}

	private void createTransformDisplays(Composite compDisplayer) {
		Composite compTransformDisplay = new Composite(compDisplayer, SWT.NONE);
		GridLayout compTransformDisplayLayout = new GridLayout(2, false);
		compTransformDisplayLayout.marginWidth = 0;
		compTransformDisplayLayout.marginHeight = 0;
		compTransformDisplay.setLayout(compTransformDisplayLayout);
		GridData compTransformDisplayLData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		compTransformDisplay.setLayoutData(compTransformDisplayLData);
		
		Label transformDisplayLabel = new Label(compTransformDisplay, SWT.NONE);
		transformDisplayLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		transformDisplay = new TextTransformationDisplayer(compTransformDisplay, layoutRoot, SWT.NONE);
		transformDisplay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
	}

	public void addObserver(Observer o) {
		this.observers.add(o);
	}

	protected void loadTextWithBtnClick() {
		TextInputWithSource textToLoad = null;
		TransformData dataToLoad = new TransformData();
		
		loadTextByWizard(textToLoad, dataToLoad);
	}


	protected void loadTextWithLinkClick() {
		TextInputWithSource textToLoad = null;
		TransformData dataToLoad = new TransformData();
		
		loadTextByWizard(textToLoad, dataToLoad);
	}

	private void loadTextByWizard(TextInputWithSource textToLoad, TransformData dataToLoad) {
		LoadTextWizard w = new LoadTextWizard(beforeWizardTextParasiteLabel, afterWizardTextParasiteLabel);
		w.setTextPageConfig(textToLoad);
		w.setTransformData(dataToLoad);
		
		WizardDialog d = new WizardDialog(this.getShell(), w);
		d.open();
		
		if(d.getReturnCode() == WizardDialog.OK) {
			TextInputWithSource loadedText = w.getTextPageConfig();
			TransformData loadedTransformation = w.getTransformData();
			
			setTextData(loadedText, loadedTransformation, true);
		}
	}

	public void setTextData(TextInputWithSource loadedText, TransformData loadedTransformation, boolean notify) {
		setVisibleJustButtonPart(loadedText == null);
		setVisibleTextSourcePart(loadedText != null);
		
		if(loadedText != null) {
			displayer.setText(loadedText);
		}
		if((this.text == null) != (loadedText == null)) {
			layoutThis();
		}

		this.text = loadedText;
		
		this.transformData = loadedTransformation==null?new TransformData():loadedTransformation;
		updateTransformationDisplay();
		if(notify) notifyObservers();
	}

	private void updateTransformationDisplay() {
		transformDisplay.setTransformData(transformData);
		setControlVisible(transformDisplay.isSomethingDisplayed(), transformDisplay);
	}

	private void layoutThis() {
		this.layoutRoot.layout(new Control[]{this});
	}

	private void setControlVisible(boolean b, Control c) {
		GridData lData = (GridData) c.getLayoutData();
		c.setVisible(b);
		lData.exclude = !b;
	}

	private void setVisibleTextSourcePart(boolean b) {
		Control c = compDisplayer;
		setControlVisible(b, c);
	}


	private void setVisibleJustButtonPart(boolean b) {
		Control c = btnLoadText;
		setControlVisible(b, c);
	}

	private void notifyObservers() {
		for(Observer o: observers) {
			o.update(null, null);
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void setControlHatcherBeforeWizText(ControlHatcher hatcherB4) {
		this.beforeWizardTextParasiteLabel = hatcherB4;
	}
	public void setControlHatcherAfterWizText(ControlHatcher hatcherAfter) {
		this.afterWizardTextParasiteLabel = hatcherAfter;
	}
}
