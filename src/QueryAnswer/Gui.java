package QueryAnswer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import gr.uoc.csd.hy463.NXMLFileReader;
import info_retrieval_system.PairHolder;
import info_retrieval_system.UserInterface;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

public class Gui extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private JLabel queryLabel;
	private JLabel typeLabel;
	private JTextField queryField;
	private JTextField typeTextField;
	
	private JPopupMenu typeSelectMenu;
	private JMenuItem diagnosis;
	private JMenuItem test;
	private JMenuItem treatment;
	
	private JTextArea statisticsArea;
	private JTextPane statisticsPane;
	
	private File selectedCollectionIndex;
	
	private Search searchEngine;
	
	private String queryText;
	private String typeFilter;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui frame = new Gui();
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
	public Gui() {
		
		// init engine
		searchEngine = new Search();
		
		// Frame
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
		
		queryField = new JTextField();
		queryField.setFont(new Font("Monospaced", Font.BOLD, 14));
		queryField.setBounds(165, 89, 209, 19);
		queryField.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(queryField);
		queryField.setColumns(10);
		
		typeTextField = new JTextField();
		typeTextField.setFont(new Font("Monospaced", Font.BOLD, 14));
		typeTextField.setBounds(165, 49, 209, 21);
		typeTextField.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		typeTextField.setEditable(false);
		contentPane.add(typeTextField);
		typeTextField.setColumns(10);
		
		// Labels
		queryLabel = new JLabel("Query:"); 
		queryLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		queryLabel.setBounds(165, 71, 78, 19);
		contentPane.add(queryLabel);
		
		typeLabel = new JLabel("Type of document:"); 
		typeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
		typeLabel.setBounds(166, 30, 150, 19);
		contentPane.add(typeLabel);
		
		// Menu
		typeSelectMenu = new JPopupMenu();
		typeSelectMenu.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		typeSelectMenu.setFont(new Font("Monospaced", Font.BOLD, 14));
		
		diagnosis = new JMenuItem("Diagnosis");
		diagnosis.setBackground(new Color(86, 23, 30));
		diagnosis.setForeground(new Color(192, 192, 192));
		diagnosis.setFont(new Font("Monospaced", Font.BOLD, 14));
		diagnosis.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		test = new JMenuItem("Test");
		test.setBackground(new Color(86, 23, 30));
		test.setForeground(new Color(192, 192, 192));
		test.setFont(new Font("Monospaced", Font.BOLD, 14));
		test.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		treatment = new JMenuItem("Treatment");
		treatment.setBackground(new Color(86, 23, 30));
		treatment.setForeground(new Color(192, 192, 192));
		treatment.setFont(new Font("Monospaced", Font.BOLD, 14));
		treatment.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		
		typeSelectMenu.add(diagnosis);
		typeSelectMenu.add(test);
		typeSelectMenu.add(treatment);
		
		// typeSelectMenu Listeners
		diagnosis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setTypeFilter("diagnosis");
                typeTextField.setText("Diagnosis");
            }
        });
		
		test.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setTypeFilter("test");
                typeTextField.setText("Test");
            }
        });
		
		treatment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setTypeFilter("treatment");
                typeTextField.setText("Treatment");
            }
        });
		
		//Buttons
		JButton typeButton = new JButton("Select");
		typeButton.setBackground(new Color(86, 23, 30));
		typeButton.setForeground(new Color(192, 192, 192));
		typeButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		typeButton.setBounds(384, 50, 67, 21);
		typeButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		
		typeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                typeSelectMenu.show(typeButton, 0, typeButton.getHeight());
            }
        });
		
		contentPane.add(typeButton);
		
		JButton searchButton = new JButton("Search");
		searchButton.setBackground(new Color(86, 23, 30));
		searchButton.setForeground(new Color(192, 192, 192));
		searchButton.setFont(new Font("Monospaced", Font.BOLD, 14));
		searchButton.setBounds(384, 89, 67, 21);
		searchButton.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		
		searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
		
		queryField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
		
		contentPane.add(searchButton);
		
		// Text Area
		/*statisticsArea = new JTextArea(15, 40);
        statisticsArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        statisticsArea.setEditable(false);*/
		
		statisticsPane = new JTextPane();
		statisticsPane.setFont(new Font("Monospaced", Font.BOLD, 15));
        statisticsPane.setContentType("text/html");
        statisticsPane.setEditable(false);
        
        // Set the preferred size of the text pane
        statisticsPane.setPreferredSize(new Dimension(450, 200));

        JScrollPane scrollPane = new JScrollPane(statisticsPane);
        scrollPane.setBounds(57, 140, 470, 230);
        scrollPane.setBorder(new LineBorder(new Color(128, 128, 128), 2, true));
		contentPane.add(scrollPane);
        
        statisticsPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String path = e.getDescription();
                    try {
						displayFileContent(path);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    System.out.println("Hyperlink clicked!");
                }
            }
        });
		
		// Logo
		JLabel logoLabel = new JLabel();
		ImageIcon imageIcon = new ImageIcon(new ImageIcon(UserInterface.class.getResource("/assets/logo.png")).getImage().getScaledInstance(120, 120, Image.SCALE_DEFAULT));
		logoLabel.setIcon(imageIcon);
		logoLabel.setHorizontalAlignment(JLabel.CENTER);
		logoLabel.setVerticalAlignment(JLabel.CENTER);
		logoLabel.setBounds(-21, -33, 214, 200);
		contentPane.add(logoLabel);

	}
	
	private void performSearch() {
		
        statisticsPane.setText("");

        if (typeTextField.getText().isEmpty() || queryField.getText().isEmpty()) {
            statisticsPane.setText("<html>Fill all the fields!<html>");
        } else {
            search();
        }
    }
	
	public void search() {
		
		TreeMap<Float, PairHolder> results = searchEngine.answerQuery(queryField.getText(), typeFilter);
		
		/*if(!results.isEmpty()) {
			statisticsArea.append("Cosine similarity rates (sorted):\n");
			int i = 1;
		    for (Map.Entry<Float, PairHolder> entry : results.entrySet()) {
		        statisticsArea.append(i + ". Document " + entry.getValue().getName() + ":\n" + "Score: " + entry.getKey() + "\nPath: " + entry.getValue().getPath() +'\n');
		        i++;
		    }
	    }else {
	    	statisticsArea.append("No matches found for the given query\n");
	    }*/
		
		if (!results.isEmpty()) {
            String htmlContent = createHtmlContent(results);
            statisticsPane.setText(htmlContent);
            statisticsPane.setCaretPosition(0);
        } else {
            statisticsPane.setText("<html>No matches found for the given query</html>");
        }
	}
	
	public String createHtmlContent(TreeMap<Float, PairHolder> results) {
	    StringBuilder sb = new StringBuilder("<html>");
	    
	    // Add the CSS styling in the <head> section
	    sb.append("<head>")
	      .append("<style>")
	      .append("body { font-family: 'Monospaced'; font-weight: bold; font-size: 10px; }")
	      .append("a { color: blue; text-decoration: underline; }") // Styling link color
	      .append("</style>")
	      .append("</head>");
	    
	    // Start the body section
	    sb.append("<body>")
	      .append("Cosine similarity rates (sorted):<br>");
	    
	    int i = 1;
	    for (Map.Entry<Float, PairHolder> entry : results.entrySet()) {
	        String docName = entry.getValue().getName();
	        float score = entry.getKey();
	        String path = entry.getValue().getPath();

	        sb.append(i).append(". Document ").append(docName).append(":<br>")
	          .append("Score: ").append(score).append("<br>")
	          .append("Path: ")
	          .append("<a href=\"").append(path).append("\">") // Create hyperlink for the path of the doc
	          .append(path).append("</a><br><br>");
	        i++;
	    }
	    
	    // End the body section and the HTML document
	    sb.append("</body>")
	      .append("</html>");
	    
	    return sb.toString();
	}

	
	private void displayFileContent(String path) throws IOException {

	    JFrame frame = new JFrame("Document - " + path);
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    frame.setSize(1000, 600);
	    frame.setLocationRelativeTo(null);

	    JTextPane textPane = new JTextPane();
	    textPane.setContentType("text/html");
	    textPane.setEditable(false);

	    File doc = new File(path);
	    NXMLFileReader xmlFile = new NXMLFileReader(doc);

	    String docID = doc.getName();
	    String title = xmlFile.getTitle();
	    String abstr = xmlFile.getAbstr();
	    String body = xmlFile.getBody();
	    String journal = xmlFile.getJournal();
	    String publisher = xmlFile.getPublisher();
	    ArrayList<String> authors = xmlFile.getAuthors();
	    HashSet<String> categories = xmlFile.getCategories();
	    
	    // Construct HTML content with modified spacing
	    String authorsList = String.join(", ", authors);
	    String categoriesList = String.join(", ", categories);
	    String htmlContent = "<html><body style='font-family: Verdena; font-size: 15px;'>" +
	        "<h1 style='margin-bottom: 10px;'>" + title + "</h1>" +
	        "<div class='section'><strong>Document ID:</strong> " + docID + "</div>" +
	        "<div class='section'><strong>Journal:</strong> " + journal + "</div>" +
	        "<div class='section'><strong>Publisher:</strong> " + publisher + "</div>" +
	        "<div class='section'><strong>Authors:</strong> " + authorsList + "</div>" +
	        "<div class='section'><strong>Categories:</strong> " + categoriesList + "</div>" +
	        "<div class='section'><h2>Abstract</h2><p style='margin-top: 5px;'>" + abstr + "</p></div>" +
	        "<div class='section'><h2>Body</h2><p style='margin-top: 5px;'>" + body + "</p></div>" +
	        "</body></html>";

	    // Set HTML content to JTextPane
	    textPane.setText(htmlContent);
	    textPane.setCaretPosition(0);

	    frame.add(new JScrollPane(textPane), BorderLayout.CENTER);
	    frame.setVisible(true);
	}

	
	public void setTypeFilter(String type) {
		this.typeFilter = type;
	}
	
	public String getTypeFilter() {
		return this.typeFilter;
	}
}
