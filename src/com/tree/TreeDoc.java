package com.tree;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class TreeDoc extends JFrame
{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel textField_RootFolder;
	private Font defaultFont = new Font("Courier New", Font.PLAIN, 12);
	private Font boldFont = new Font("Courier New", Font.BOLD, 12);
	private JCheckBox includeFiles = new JCheckBox("Include Files");
	private File rootFolder;
	private String version = "1.70";
	private JTextField textFilter;
	private JTextArea textExcludeFolders = new JTextArea();
	private JTextArea textExcludeFiles = new JTextArea();
	final Logger logger = org.apache.logging.log4j.LogManager.getLogger(TreeDoc.class);

	public static void main(String[] args)
	{

		TreeDoc frame = new TreeDoc();

		GraphicsDevice gd = JUtility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		frame.setBounds(screenBounds.x + ((screenBounds.width - frame.getWidth()) / 2), screenBounds.y + ((screenBounds.height - frame.getHeight()) / 2), frame.getWidth(), frame.getHeight());

		frame.setVisible(true);
	}

	public TreeDoc()
	{
		try
		{
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{

		}

		setFont(defaultFont);
		setTitle("Tree Documentor" + " " + version);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 900, 572);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(211, 211, 211));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setBackground(UIManager.getColor("Button.highlight"));
		desktopPane.setBounds(0, 0, 900, 544);
		contentPane.add(desktopPane);
		desktopPane.setLayout(null);

		JScrollPane scrollPane_Directory = new JScrollPane();
		scrollPane_Directory.setBounds(12, 77, 660, 457);
		desktopPane.add(scrollPane_Directory);

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(new Color(255, 0, 0));
		textArea.setFont(defaultFont);
		textArea.setBackground(SystemColor.window);
		textArea.setBounds(0, 0, 7, 21);
		textArea.setBorder(BorderFactory.createLineBorder(Color.black));
		scrollPane_Directory.setViewportView(textArea);

		JButton btnBrowse = new JButton("Select Folder");
		btnBrowse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int option = fileChooser.showOpenDialog(TreeDoc.this);

				if (option == JFileChooser.APPROVE_OPTION)
				{
					rootFolder = fileChooser.getSelectedFile();
					textField_RootFolder.setText(rootFolder.getAbsolutePath());
					textArea.setText(TreeNode.displayTree(rootFolder, includeFiles.isSelected(), textFilter.getText(), textExcludeFolders.getText(), textExcludeFiles.getText()));
					textArea.setCaretPosition(0);
				}

			}
		});
		btnBrowse.setFont(boldFont);
		btnBrowse.setBounds(12, 10, 131, 25);
		desktopPane.add(btnBrowse);

		textField_RootFolder = new JLabel();
		textField_RootFolder.setForeground(new Color(0, 0, 255));
		textField_RootFolder.setFont(defaultFont);
		textField_RootFolder.setBounds(122, 45, 550, 25);
		textField_RootFolder.setOpaque(true);
		textField_RootFolder.setBackground(SystemColor.window);
		textField_RootFolder.setBorder(BorderFactory.createLineBorder(Color.black));
		desktopPane.add(textField_RootFolder);

		JButton btnSave = new JButton("Save to File");
		btnSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser jFileChooser = new JFileChooser();

				jFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

				jFileChooser.setSelectedFile(new File("tree.txt"));

				int returnVal = jFileChooser.showSaveDialog(TreeDoc.this);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					try
					{
						File newTextFile = new File(jFileChooser.getSelectedFile().getAbsolutePath());

						FileOutputStream fos = new FileOutputStream(newTextFile);
						OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
						BufferedWriter writer = new BufferedWriter(osw);

						writer.write(textArea.getText());
						writer.flush();
						writer.close();
					}
					catch (IOException iox)
					{
						iox.printStackTrace();
					}
				}
			}
		});
		btnSave.setFont(boldFont);
		btnSave.setBounds(278, 10, 131, 25);
		desktopPane.add(btnSave);

		JButton btnQuit = new JButton("Quit");
		btnQuit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveConfig();
				System.exit(0);
			}
		});
		btnQuit.setFont(boldFont);
		btnQuit.setBounds(543, 10, 131, 25);
		desktopPane.add(btnQuit);

		JButton btnClearOutput = new JButton("Clear Output");
		btnClearOutput.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				textArea.setText("");
			}
		});
		btnClearOutput.setFont(boldFont);
		btnClearOutput.setBounds(410, 10, 131, 25);
		desktopPane.add(btnClearOutput);
		includeFiles.setSelected(true);
		includeFiles.setBackground(UIManager.getColor("Button.highlight"));

		includeFiles.setBounds(692, 9, 117, 23);
		desktopPane.add(includeFiles);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (rootFolder != null)
				{
					textArea.setText("");
					textArea.setText(TreeNode.displayTree(rootFolder, includeFiles.isSelected(), textFilter.getText(), textExcludeFolders.getText(), textExcludeFiles.getText()));
					textArea.setCaretPosition(0);
				}
			}
		});
		btnRefresh.setBounds(145, 10, 131, 25);
		desktopPane.add(btnRefresh);

		JLabel lblRootFolder = new JLabel("Root Folder :");
		lblRootFolder.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRootFolder.setBounds(12, 50, 103, 15);
		desktopPane.add(lblRootFolder);

		textFilter = new JTextField();
		textFilter.setBounds(684, 45, 84, 25);
		desktopPane.add(textFilter);
		textFilter.setColumns(10);

		JLabel lblFilter = new JLabel("Include File Ext");
		lblFilter.setBounds(771, 45, 102, 25);
		desktopPane.add(lblFilter);

		JScrollPane scrollPane_ExcludeFolders = new JScrollPane();
		scrollPane_ExcludeFolders.setBounds(684, 100, 204, 202);
		desktopPane.add(scrollPane_ExcludeFolders);

		textExcludeFolders.setLocation(681, 0);
		scrollPane_ExcludeFolders.setViewportView(textExcludeFolders);

		JLabel lblExcludeFolders = new JLabel("Exclude Folders");
		lblExcludeFolders.setBounds(684, 77, 204, 25);
		desktopPane.add(lblExcludeFolders);

		JLabel lblExcludeFiles = new JLabel("Exclude Files");
		lblExcludeFiles.setBounds(684, 310, 204, 25);
		desktopPane.add(lblExcludeFiles);

		JScrollPane scrollPane_ExcludeFiles = new JScrollPane();
		scrollPane_ExcludeFiles.setBounds(684, 332, 204, 202);
		desktopPane.add(scrollPane_ExcludeFiles);

		scrollPane_ExcludeFiles.setViewportView(textExcludeFiles);

		loadConfig();

		rootFolder = new File(textField_RootFolder.getText());
		if (rootFolder.exists() == false)
		{

		}

		textArea.setText(TreeNode.displayTree(rootFolder, includeFiles.isSelected(), textFilter.getText(), textExcludeFolders.getText(), textExcludeFiles.getText()));
		textArea.setCaretPosition(0);
	}

	private void loadConfig()
	{

		JXMLDocument xmlMessage = new JXMLDocument();
		String rootFolder = System.getProperty("user.dir");
		String includeFilesStr = "N";
		String fileExtension = "";
		String folders = "";
		String files = "";

		boolean result = xmlMessage.setDocument("." + File.separator + "xml" + File.separator + "config" + File.separator + "config.xml");

		if (result)
		{
			rootFolder = xmlMessage.findXPath("//TreeDoc/rootFolder").trim();

			Path temp = Paths.get(rootFolder);
			boolean valid = Files.exists(temp);

			if (valid)
			{
				valid = org.apache.commons.io.FileUtils.isDirectory(new File(rootFolder), LinkOption.NOFOLLOW_LINKS);
			}

			if (valid == false)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int option = fileChooser.showOpenDialog(TreeDoc.this);

				if (option == JFileChooser.APPROVE_OPTION)
				{
					rootFolder = fileChooser.getSelectedFile().getAbsolutePath();
				}
				else
				{
					System.exit(0);
				}
			}

			includeFilesStr = xmlMessage.findXPath("//TreeDoc/includeFiles").trim().toUpperCase();

			fileExtension = xmlMessage.findXPath("//TreeDoc/fileExtension").trim();

			boolean more = true;
			int occur = 1;

			while (more)
			{
				String folder = xmlMessage.findXPath("//TreeDoc/excludeFolders/folder[" + occur + "]");
				if (folder.isBlank())
				{
					more = false;
				}
				else
				{
					folders = folders + folder + "\n";
				}
				occur++;
			}

			more = true;
			occur = 1;

			while (more)
			{
				String file = xmlMessage.findXPath("//TreeDoc/excludeFiles/file[" + occur + "]");
				if (file.isBlank())
				{
					more = false;
				}
				else
				{
					files = files + file + "\n";
				}
				occur++;
			}

		}
		else
		{
			logger.error("Cannot read config.xml");
		}

		if (rootFolder.isBlank())
		{
			rootFolder = System.getProperty("user.home");
		}

		if (org.apache.commons.io.FileUtils.isDirectory(new File(rootFolder), LinkOption.NOFOLLOW_LINKS) == false)
		{
			rootFolder = System.getProperty("user.home");
		}

		textField_RootFolder.setText(rootFolder);

		if (includeFilesStr.isBlank())
		{
			includeFilesStr = "N";
		}

		if (includeFilesStr.equals("Y"))
		{
			includeFiles.setSelected(true);
		}
		else
		{
			includeFiles.setSelected(false);
		}

		textFilter.setText(fileExtension);

		textExcludeFolders.setText(folders);

		textExcludeFiles.setText(files);
	}

	private void saveConfig()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		final JFileIO fio = new JFileIO();

		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document document = builder.newDocument();

			Element TreeDoc = (Element) document.createElement("TreeDoc");

			Element rootFolder = (Element) document.createElement("rootFolder");
			Text text = document.createTextNode(textField_RootFolder.getText());
			rootFolder.appendChild(text);
			TreeDoc.appendChild(rootFolder);

			Element includeFilesStr = (Element) document.createElement("includeFiles");
			if (includeFiles.isSelected())
			{
				text = document.createTextNode("Y");
			}
			else
			{
				text = document.createTextNode("N");
			}
			includeFilesStr.appendChild(text);
			TreeDoc.appendChild(includeFilesStr);

			Element fileExtension = (Element) document.createElement("fileExtension");
			text = document.createTextNode(textFilter.getText());
			fileExtension.appendChild(text);
			TreeDoc.appendChild(fileExtension);

			String[] foldersString = textExcludeFolders.getText().split("\n");

			Element excludeFolders = (Element) document.createElement("excludeFolders");

			for (int x = 0; x < foldersString.length; x++)
			{
				Element excludeFolder = (Element) document.createElement("folder");
				text = document.createTextNode(foldersString[x]);
				excludeFolder.appendChild(text);
				excludeFolders.appendChild(excludeFolder);
			}
			TreeDoc.appendChild(excludeFolders);

			String[] filesString = textExcludeFiles.getText().split("\n");

			Element excludeFiles = (Element) document.createElement("excludeFiles");

			for (int x = 0; x < filesString.length; x++)
			{
				Element excludeFile = (Element) document.createElement("file");
				text = document.createTextNode(filesString[x]);
				excludeFile.appendChild(text);
				excludeFiles.appendChild(excludeFile);
			}

			TreeDoc.appendChild(excludeFiles);

			document.appendChild(TreeDoc);

			fio.writeToDisk("." + File.separator + "xml" + File.separator + "config" + File.separator + "config.xml", document);
		}
		catch (ParserConfigurationException pce)
		{
			// Parser with specified options can't be built
			pce.printStackTrace();
		}

		catch (Exception ex)
		{
			logger.error("Cannot write config.xml to file");
		}

	}
}
