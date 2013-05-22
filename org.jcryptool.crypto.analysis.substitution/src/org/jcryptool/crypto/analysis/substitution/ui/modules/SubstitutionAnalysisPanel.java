package org.jcryptool.crypto.analysis.substitution.ui.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jcryptool.core.operations.alphabets.AbstractAlphabet;
import org.jcryptool.crypto.analysis.substitution.calc.TextStatistic;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.StatisticsDisplayer;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.StatisticsWizard;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.SubstitutionAnalysisText;
import org.jcryptool.crypto.analysis.substitution.ui.modules.utils.SubstitutionKeyEditor;
import org.jcryptool.crypto.classic.substitution.algorithm.SubstitutionKey;
import org.jcryptool.crypto.classic.substitution.algorithm.SubstitutionKeyValidityException;
import org.jcryptool.crypto.ui.textsource.TextInputWithSourceDisplayer;
import org.jcryptool.editor.text.JCTTextEditorPlugin;

public class SubstitutionAnalysisPanel extends Composite {

	private static final int MAIN_LBL_WIDTH_HINT = 250;
	private AbstractAlphabet alphabet;
	private TextStatistic referenceStatistic;
	private String text;
	private TextStatistic cipherStatistic;
	private StatisticsDisplayer statisticsDisplayer;
	private SubstitutionKeyEditor substEditor;
	private boolean upperLowerCipherMode;
	private SubstitutionAnalysisText previewer;
	private Text k1PreviewText;
	private Text k2PreviewText;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public SubstitutionAnalysisPanel(Composite parent, int style, String text, AbstractAlphabet alphabet, TextStatistic referenceStatistic, boolean upperLowerCipherMode) {
		super(parent, style);
		this.text = text;
		this.alphabet = alphabet;
		this.referenceStatistic = referenceStatistic;
		this.upperLowerCipherMode = upperLowerCipherMode;
		this.cipherStatistic = new TextStatistic(text);
		
		initGUI();

	}

	private void initGUI() {
		setLayout(new GridLayout(2, false));
		
		Group grpSubstitutionAnalysis = new Group(this, SWT.NONE);
		grpSubstitutionAnalysis.setText("Substitution analysis");
		grpSubstitutionAnalysis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSubstitutionAnalysis.setLayout(new GridLayout(1, false));
		
		initHelpGroup(grpSubstitutionAnalysis);
		
		Group grpLetterFrequencyStatistics = new Group(this, SWT.NONE);
		grpLetterFrequencyStatistics.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 3));
		grpLetterFrequencyStatistics.setText("Letter frequency statistics");
		grpLetterFrequencyStatistics.setLayout(new GridLayout(1, false));
		
		initLetterFreqGroup(grpLetterFrequencyStatistics);
		
		Group grpSubstitutions = new Group(this, SWT.NONE);
		grpSubstitutions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSubstitutions.setText("Substitutions");
		grpSubstitutions.setLayout(new GridLayout(1, false));
		
		initSubstGroup(grpSubstitutions);
		
		Group grpPreviewOfThe = new Group(this, SWT.NONE);
		grpPreviewOfThe.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpPreviewOfThe.setText("Preview of the deciphered text");
		grpPreviewOfThe.setLayout(new GridLayout(1, false));
		
		initPreviewGroup(grpPreviewOfThe);
	}

	private void initHelpGroup(Group grpSubstitutionAnalysis) {
		Label lblTheSubstitutionAnalysis = new Label(grpSubstitutionAnalysis, SWT.WRAP);
		GridData lblTheSubstitutionAnalysisLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		lblTheSubstitutionAnalysisLayoutData.widthHint = MAIN_LBL_WIDTH_HINT;
		lblTheSubstitutionAnalysis.setLayoutData(lblTheSubstitutionAnalysisLayoutData);
		lblTheSubstitutionAnalysis.setText("The substitution analysis consists of figuring out, which characters in the ciphertext have to be replaced by which former plaintext characters.\r\nThe reference- and ciphertext statistics help you with that. Single characters/pairs/triplets with similar frequencies very possible correspond to each other. Try to substitute these reference characters with their corresponding ciphertext characters with the substitution editor below, and observe how the plaintext changes.");
	}

	private void initPreviewGroup(Group grpPreviewOfThe) {
		previewer = new SubstitutionAnalysisText(grpPreviewOfThe, SWT.NONE, text, upperLowerCipherMode);
		previewer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		
		Button exportButton = new Button(grpPreviewOfThe, SWT.PUSH);
		exportButton.setText("Open the plaintext in a new editor");
		Image image = JCTTextEditorPlugin.getDefault().getImageRegistry()
                        .get(JCTTextEditorPlugin.JCT_TEXT_EDITOR_ICON);
		exportButton.setImage(image);
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String calcPlaintext = calculatePlaintext();
				TextInputWithSourceDisplayer.openTextInEditor(calcPlaintext, "substAnalysisOutput.txt");
			}
		});
	}

	protected String calculatePlaintext() {
		return previewer.getLastPlaintext();
	}

	private void initSubstGroup(Group grpSubstitutions) {
		substEditor = new SubstitutionKeyEditor(grpSubstitutions, SWT.NONE, alphabet);
		substEditor.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		
		substEditor.setCharMappingExternal(new HashMap<Character, Character>());
		substEditor.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				preview();
			}
		});
		
		Composite keyPreview = new Composite(grpSubstitutions, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		keyPreview.setLayout(layout);
		keyPreview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label lblKeyPreview = new Label(keyPreview, SWT.WRAP);
		GridData lblKeyPreviewLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		lblKeyPreviewLayoutData.widthHint = MAIN_LBL_WIDTH_HINT;
		lblKeyPreview.setLayoutData(lblKeyPreviewLayoutData);
		lblKeyPreview.setText("Below, the substitution will be displayed as String. In the first field, the substitution as set above will be shown; This is the substitution for ciphertext to plaintext conversion. A '?' marks all positions where a substitution was not yet set (fields outlined red above)\nThe other direction (plaintext to ciphertext) will be displayed in the second field, when possible (when every character in the editor above has got a substitution set).");
		
		Label k1PreviewLabel = new Label(keyPreview, SWT.NONE);
		k1PreviewLabel.setText("cipher to plaintext substitution:");
		
		k1PreviewText = new Text(keyPreview, SWT.BORDER);
		k1PreviewText.setEditable(false);
		k1PreviewText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		k1PreviewText.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));

		Label k2PreviewLabel = new Label(keyPreview, SWT.NONE);
		k2PreviewLabel.setText("plaintext to cipher substitution:");
		
		k2PreviewText = new Text(keyPreview, SWT.BORDER);
		k2PreviewText.setEditable(false);
		k2PreviewText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		k2PreviewText.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		
	}

	protected void preview() {
		Map<Character, Character> mapping = substEditor.getCharMapping();
		previewer.setMapping(mapping);
		
		Map<Character, Character> key1Data = substEditor.getCharMapping();
		String key1String = generateKey1String(key1Data, alphabet);
		
		String key2String = "<The substitutions must be completely filled out>";
		if(substEditor.isCompleteData()) {
			try {
				SubstitutionKey key = new SubstitutionKey(SubstitutionKey.invertSubstitution(key1Data));
				key2String = key.toStringSubstitutionCharList(alphabet);
			} catch (SubstitutionKeyValidityException e) {
				e.printStackTrace();
			}
		}
		
		k1PreviewText.setText(key1String);
		k2PreviewText.setText(key2String);
		
	}

	private String generateKey1String(Map<Character, Character> key1Data, AbstractAlphabet alphabet) {
		StringBuilder sb = new StringBuilder();
		for(char c: alphabet.getCharacterSet()) {
			Character mapping = key1Data.get(c);
			if(mapping != null) {
				sb.append(mapping);
			} else {
				sb.append('?');
			}
		}
		return sb.toString();
	}

	private void initLetterFreqGroup(final Group grpLetterFrequencyStatistics) {
		statisticsDisplayer = new StatisticsDisplayer(grpLetterFrequencyStatistics, SWT.NONE, referenceStatistic, cipherStatistic);
		statisticsDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		final Button loadStatisticsButton = new Button(grpLetterFrequencyStatistics, SWT.PUSH);
		loadStatisticsButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		loadStatisticsButton.setText("Load another reference statistic");
		loadStatisticsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StatisticsWizard w = new StatisticsWizard(alphabet);
				WizardDialog d = new WizardDialog(getShell(), w);
				int result = d.open();
				
				if(result == WizardDialog.OK) {
					TextStatistic stat = w.getStatistics();
					
					if(stat != null) {
						SubstitutionAnalysisPanel.this.referenceStatistic = stat;

						statisticsDisplayer.dispose();
						loadStatisticsButton.dispose();
						
						initLetterFreqGroup(grpLetterFrequencyStatistics);
						SubstitutionAnalysisPanel.this.layout(new Control[]{statisticsDisplayer});
					}
				}
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
