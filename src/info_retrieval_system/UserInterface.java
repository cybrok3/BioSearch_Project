package info_retrieval_system;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Toolkit;

public class UserInterface extends JFrame implements ProgressObserver{
	
	private JPanel contentPane;
	private JTextField collectionPathField;
	private JTextField indexPathField;
	private JTextField stopwordsPathField;
	private JTextArea statisticsArea;
	
	private File selectedCollectionFolder;
    private File selectedIndexFolder;
    private File selectedStopwordsFolder;
    private JLabel collectionLabel;
    private JLabel indexLabel;
    private JLabel stopwordsLabel;
    private JProgressBar progressBar;
    
    private int thresholdValue = 10200;
    
    private Color MAROON = new Color(86, 23, 30);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserInterface frame = new UserInterface();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UserInterface() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(UserInterface.class.getResource("/assets/frame_icon.png")));
		setResizable(false);
		
		setTitle("BioSearch");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 598, 443);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(210, 210, 210));
		contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		collectionPathField = new JTextField();
		collectionPathField.setFont(new Font("Monospaced", Font.BOLD, 14));
		collectionPathField.setBounds(153, 26, 275, 19);
		collectionPathField.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(collectionPathField);
		collectionPathField.setColumns(10);
		
		indexPathField = new JTextField();
		indexPathField.setFont(new Font("Monospaced", Font.BOLD, 14));
		indexPathField.setBounds(153, 66, 275, 19);
		indexPathField.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(indexPathField);
		indexPathField.setColumns(10);
		
		stopwordsPathField = new JTextField();
		stopwordsPathField.setFont(new Font("Monospaced", Font.BOLD, 14));
		stopwordsPathField.setBounds(154, 106, 275, 19);
		stopwordsPathField.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(stopwordsPathField);
		stopwordsPathField.setColumns(10);
		
		JButton collectionBrowseButton = new JButton("Select");
		collectionBrowseButton.setBackground(new Color(255, 255, 255));
		collectionBrowseButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		collectionBrowseButton.setBounds(439, 25, 85, 21);
		collectionBrowseButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(collectionBrowseButton);
		collectionBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showOpenDialog(UserInterface.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedCollectionFolder = chooser.getSelectedFile();
                    collectionPathField.setText(selectedCollectionFolder.getAbsolutePath());
                }
            }
        });
		
		JButton indexBrowseButton = new JButton("Select");
		indexBrowseButton.setBackground(new Color(255, 255, 255));
		indexBrowseButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		indexBrowseButton.setBounds(438, 65, 85, 21);
		indexBrowseButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(indexBrowseButton);
		indexBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showOpenDialog(UserInterface.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedIndexFolder = chooser.getSelectedFile();
                    indexPathField.setText(selectedIndexFolder.getAbsolutePath());
                }
            }
        });
		
		JButton stopwordsBrowseButton = new JButton("Select");
		stopwordsBrowseButton.setBackground(new Color(255, 255, 255));
		stopwordsBrowseButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		stopwordsBrowseButton.setBounds(438, 105, 85, 21);
		stopwordsBrowseButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(stopwordsBrowseButton);
		stopwordsBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showOpenDialog(UserInterface.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedStopwordsFolder = chooser.getSelectedFile();
                    stopwordsPathField.setText(selectedStopwordsFolder.getAbsolutePath());
                }
            }
        });
		
		JButton startButton = new JButton("Start");
		startButton.setBackground(new Color(255, 255, 255));
		startButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		startButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		startButton.setBounds(249, 347, 85, 21);
		contentPane.add(startButton);
        startButton.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	if(selectedCollectionFolder == null) {
            		if(!collectionPathField.getText().isEmpty()) {
            			selectedCollectionFolder = new File(collectionPathField.getText());
            		}
    			}
    			
    			if(selectedIndexFolder == null) {
    				if(!indexPathField.getText().isEmpty()) {
    					selectedIndexFolder = new File(indexPathField.getText());
            		}
    			}
    			
    			if(selectedStopwordsFolder == null) {
    				if(!stopwordsPathField.getText().isEmpty()) {
    					selectedStopwordsFolder = new File(stopwordsPathField.getText());
            		}
    			}
            	
                startIndexing();
            }
        });
        
        JButton clearInfoButton = new JButton("Clear");
		clearInfoButton.setBackground(new Color(255, 255, 255));
		clearInfoButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		clearInfoButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		clearInfoButton.setBounds(57, 347, 85, 21);
		contentPane.add(clearInfoButton);
		clearInfoButton.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
            	statisticsArea.setText(null);
            }
        });
        
        statisticsArea = new JTextArea(15, 40);
        statisticsArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        
        JScrollPane scrollPane = new JScrollPane(statisticsArea);
		scrollPane.setBounds(57, 161, 470, 179);
		scrollPane.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));;
		contentPane.add(scrollPane);

		collectionLabel = new JLabel("Collection Folder:");
		collectionLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		collectionLabel.setBounds(153, 9, 275, 13);
		contentPane.add(collectionLabel);
		
		indexLabel = new JLabel("Index Folder:");
		indexLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		indexLabel.setBounds(153, 49, 275, 13);
		contentPane.add(indexLabel);
		
		stopwordsLabel = new JLabel("Stopwords Folder:");
		stopwordsLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		stopwordsLabel.setBounds(153, 89, 275, 13);
		contentPane.add(stopwordsLabel);
		
		progressBar = new JProgressBar();
		progressBar.setBackground(new Color(255, 255, 255));
		progressBar.setFont(new Font("Monospaced", Font.BOLD, 10));
		progressBar.setValue(0);
		progressBar.setBounds(203, 136, 177, 15);
		progressBar.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		progressBar.setForeground(MAROON);
		contentPane.add(progressBar);
		
		JLabel logoLabel = new JLabel();
		ImageIcon imageIcon = new ImageIcon(new ImageIcon(UserInterface.class.getResource("/assets/logo.png")).getImage().getScaledInstance(120, 120, Image.SCALE_DEFAULT));
		logoLabel.setIcon(imageIcon);
		logoLabel.setHorizontalAlignment(JLabel.CENTER);
		logoLabel.setVerticalAlignment(JLabel.CENTER);
		logoLabel.setBounds(-27, -30, 214, 200);
		contentPane.add(logoLabel);
		
		JLabel lblNewLabel = new JLabel("Powered by CS-463");
		lblNewLabel.setForeground(new Color(0, 48, 82));
		lblNewLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		lblNewLabel.setBounds(376, 350, 153, 13);
		contentPane.add(lblNewLabel);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(MAROON);
		
		JMenu optionsMenu = new JMenu("Options");
		optionsMenu.setForeground(new Color(192, 192, 192));
		optionsMenu.setFont(new Font("Monospaced", Font.BOLD, 14));
		optionsMenu.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setForeground(new Color(192, 192, 192));
		helpMenu.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		helpMenu.setFont(new Font("Monospaced", Font.BOLD, 14));
		
		JMenu aboutMenu = new JMenu("About");
		aboutMenu.setForeground(new Color(192, 192, 192));
		aboutMenu.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		aboutMenu.setFont(new Font("Monospaced", Font.BOLD, 14));
		
		JMenu programMenu = new JMenu("Program");
		programMenu.setForeground(new Color(192, 192, 192));
		programMenu.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		programMenu.setFont(new Font("Monospaced", Font.BOLD, 14));
		
		JMenuItem chooseThreshold = new JMenuItem("Choose threshold");
		chooseThreshold.setBackground(new Color(86, 23, 30));
		chooseThreshold.setForeground(new Color(192, 192, 192));
		chooseThreshold.setFont(new Font("Monospaced", Font.BOLD, 14));
		chooseThreshold.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		JMenuItem helpItem = new JMenuItem("Instructions");
		helpItem.setBackground(new Color(86, 23, 30));
		helpItem.setForeground(new Color(192, 192, 192));
		helpItem.setFont(new Font("Monospaced", Font.BOLD, 14));
		helpItem.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		JMenuItem aboutItem = new JMenuItem("Developer");
		aboutItem.setBackground(new Color(86, 23, 30));
		aboutItem.setForeground(new Color(192, 192, 192));
		aboutItem.setFont(new Font("Monospaced", Font.BOLD, 14));
		aboutItem.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		JMenuItem restartItem = new JMenuItem("Restart");
		restartItem.setBackground(new Color(86, 23, 30));
		restartItem.setForeground(new Color(192, 192, 192));
		restartItem.setFont(new Font("Monospaced", Font.BOLD, 14));
		restartItem.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setBackground(new Color(86, 23, 30));
		closeItem.setForeground(new Color(192, 192, 192));
		closeItem.setFont(new Font("Monospaced", Font.BOLD, 14));
		closeItem.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
        
		optionsMenu.add(chooseThreshold);
		helpMenu.add(helpItem);
		aboutMenu.add(aboutItem);
		programMenu.add(restartItem);
		programMenu.add(closeItem);
		
		menuBar.add(optionsMenu);
		menuBar.add(helpMenu);
		menuBar.add(aboutMenu);
		menuBar.add(programMenu);
		menuBar.setBorder(new LineBorder(new Color(86, 23, 30), 2));
		
		setJMenuBar(menuBar);
		
		chooseThreshold.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String input = JOptionPane.showInputDialog("Enter Threshold value:");
                try {
                    int threshold = Integer.parseInt(input);
                    setThreshold(threshold);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "<html><body width='450'>Default value of 10200 records will remain.");
                    setThreshold(10200);
                }
            }
        });
		
		helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelpDialog();
            }
        });
		
		aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
		
		restartItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        // Dispose of the current frame
		        dispose();
		        
		        // Restart the program
		        main(new String[] {});
		    }
		});
		
		closeItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        System.exit(0);
		    }
		});
		
		UIManager.put("OptionPane.background", new Color(192, 192, 192));
        UIManager.put("OptionPane.border", BorderFactory.createLineBorder(MAROON, 5));
        UIManager.put("Panel.background", new Color(192, 192, 192));
        UIManager.put("OptionPane.messageFont", new java.awt.Font("Monospaced", java.awt.Font.BOLD, 16));
        UIManager.put("OptionPane.messageForeground", new java.awt.Color(0, 0, 0));
        
        UIManager.put("Button.background", MAROON);
        UIManager.put("Button.foreground", new java.awt.Color(192, 192, 192));
        UIManager.put("Button.font", new java.awt.Font("Monospaced", java.awt.Font.BOLD, 14));
        UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(128, 128, 128), 2));
        
        UIManager.put("TextField.font", new Font("Monospaced", Font.BOLD, 14));
		
	}
	
	private void startIndexing() {
		
		if (selectedCollectionFolder == null || selectedIndexFolder == null || selectedStopwordsFolder == null) {
            JOptionPane.showMessageDialog(this, "<html><body width='450'>Please select all required folders.\nIn case you didn't, use the Select buttons\nor enter paths manually.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
		
		String indexFolderName = "CollectionIndex";
		
		Path collectionIndexPath = Paths.get(indexPathField.getText(), "CollectionIndex");
		
		if(Files.exists(collectionIndexPath)) {
			int choice = showFolderNameDialog();
			
			if (choice == 0) {
				try {
		            Files.walk(collectionIndexPath)
		                .sorted(Comparator.reverseOrder())
		                .map(Path::toFile)
		                .forEach(File::delete);
		            
		            Files.createDirectory(collectionIndexPath);
		        } catch (IOException ex) {
		            ex.printStackTrace();
		        }
			}else {
		        String newFolderName = JOptionPane.showInputDialog(
		            this,
		            "Please enter a new name for the folder:",
		            "New Folder Name",
		            JOptionPane.PLAIN_MESSAGE
		        );

		        if (newFolderName != null && !newFolderName.trim().isEmpty()) {
		        	indexFolderName = newFolderName;
		        }
			}
		}
		
		statisticsArea.append("Collection: " + selectedCollectionFolder.getName() + " with threshold = " + this.thresholdValue + '\n');
		
        
        statisticsArea.append("Writting data...\n");
        
        // Call your indexing methods here
        PartialIndexing indexer = new PartialIndexing(this, selectedCollectionFolder.getAbsolutePath(), selectedIndexFolder.getAbsolutePath(), selectedStopwordsFolder.getAbsolutePath(), indexFolderName, thresholdValue);
        
        try {
            indexer.indexCollection();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

	@Override
	public void initProgressbar(int endValue) {
		progressBar.setMaximum(endValue);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("");
	}

	@Override
	public void updateDataWriteProgressBar(int progress) {
			progressBar.setValue(progress);
			progressBar.setString(progress + " / " + progressBar.getMaximum() + " Files done");
	        if(progress == progressBar.getMaximum()) {
	        	statisticsArea.append("Writting data completed\n");
	        }
	}

	@Override
	public void updateMergingProgressBar(int progress) {
	        progressBar.setValue(progress);
	        progressBar.setString(progress + " / " + progressBar.getMaximum() + " Merges done");
	        if (progress == progressBar.getMaximum()) {
	            statisticsArea.append("Merging completed\n");
	            progressBar.setValue(0);
	            progressBar.setStringPainted(false);
	        }
	}
	
	public void updateNormsCalculationProgressBar(int progress) {
		progressBar.setValue(progress);
        progressBar.setString("Calculating norms..");
        if (progress == progressBar.getMaximum()) {
            statisticsArea.append("Calculating norms completed\n");
            progressBar.setValue(0);
        }
	}

	
	@Override
	public void printDataWriteStatistics(long exeTime, int docsProcessed) {
        statisticsArea.append("Data writing time: " + exeTime + " milliseconds\n");
        statisticsArea.append("Files processed: " + docsProcessed + "\n");
        statisticsArea.append("Merging...\n");
	}

	@Override
	public void printMergingStatistics(long exeTime, int mergesDone) {
		statisticsArea.append("Merge time: " + exeTime + " milliseconds\n");
        statisticsArea.append("Merges done: " + mergesDone + "\n");
        progressBar.setValue(0);
        statisticsArea.append("Calculating norms...\n");
	}
	
	@Override
	public void printNormCalculationStatistics(long exeTime) {
		statisticsArea.append("Norms calculation time: " + exeTime + " milliseconds\n\n");
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
	}
	
	private void showHelpDialog() {
        String message = "<html><body width='950'>" + "Instructions:\n\n"
	             + "1) Select Collection Folder:\n"
	             + "Choose the folder that contains the medical collection with the nxml files.\n\n"
	             + "2) Select Index Folder:\n"
	             + "Choose where you want to create the CollectionIndex folder that will contain the IndexedFile.\n\n"
	             + "3) Select Stopwords Folder:\n"
	             + "Choose the folder that contains the txt file with all the stopwords that must be ignored.\n\n"
	             + "4) Options Menu:\n" 
	             + "On the options menu select Choose Threshold to manually set a threshold greater than the default\n"
	             + "value 10200 records any value < 10200 records will be ignored and the default value will remain,\n"
	             + "consider that high threshold value will result in less partial Indexes being created thus less\n"
	             + "time taken to execute (Be aware of physical memory usage).";
        try {
	        JOptionPane.showMessageDialog(this, message, "Instructions", JOptionPane.INFORMATION_MESSAGE);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }
	
	public void showAboutDialog(){
		String message = "<html><body width='300'>" +
                "Name: Anastasios Milios<br>" +
                "ID: CSD 4945<br>" +
                "Solo Project Phase A</body></html>";
		
		try {
	        JOptionPane.showMessageDialog(this, message, "Developer", JOptionPane.INFORMATION_MESSAGE);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public int showFolderNameDialog() {
		String message =
                "There is already a folder named CollectionIndex at the provided indexFolder path.\n" +
                "Would you like to replace it or keep it and create another indexFolder with a new name?";
		
		 String[] options = {"Replace", "New Folder"};

		    int choice = JOptionPane.showOptionDialog(
		        this,
		        message,
		        "IndexFolder",
		        JOptionPane.YES_NO_OPTION,
		        JOptionPane.QUESTION_MESSAGE,
		        null,
		        options,
		        options[0]
		    );

		    return choice; // Returns 0 for Replace Existing Folder, 1 for Create New Folder
	}
	
	public void setThreshold(int value) {
		this.thresholdValue = value;
	}
}
