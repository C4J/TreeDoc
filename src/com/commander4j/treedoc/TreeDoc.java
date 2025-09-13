package com.commander4j.treedoc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.commander4j.dialog.JDialogAbout;
import com.commander4j.dialog.JDialogLicenses;
import com.commander4j.gui.widgets.JButton4j;
import com.commander4j.gui.widgets.JCheckBox4j;
import com.commander4j.gui.widgets.JLabel4j_std;
import com.commander4j.gui.widgets.JSpinner4j;
import com.commander4j.gui.widgets.JTextField4j;
import com.commander4j.settings.Common;
import com.commander4j.util.JFileIO;
import com.commander4j.util.JHelp;
import com.commander4j.util.JImageIconLoader;
import com.commander4j.util.JUtility;
import com.commander4j.util.JXMLDocument;

public class TreeDoc extends JFrame
{

	private static final long serialVersionUID = 1L;
	private final JTextField4j pathField = new JTextField4j();
	private final JCheckBox4j includeFilesChk = new JCheckBox4j("Include files");
	private final JSpinner4j depthSpinner = new JSpinner4j(new SpinnerNumberModel(9999, 0, 9999, 1));
	private final JSpinner4j entriesSpinner = new JSpinner4j(new SpinnerNumberModel(10000, 100, 1_000_000, 100));
	private final JTextArea outputArea = new JTextArea(40, 100);
	public static Logger logger = org.apache.logging.log4j.LogManager.getLogger(TreeDoc.class);
	public static LoggerContextFactory factory = LogManager.getFactory();
	public static String version = "2.00";
	private Dimension btn = new Dimension(32, 32);
	private Dimension separator = new Dimension(10, 10);
	private JUtility util = new JUtility();
	private JToolBar statusBar = new JToolBar();
	private JLabel4j_std statusMessage = new JLabel4j_std("");
	public static final JImageIconLoader imageIconloader = new JImageIconLoader();
	public ImageIcon logo = imageIconloader.getImageIcon(Common.icon_logo);

	public TreeDoc()
	{
		super();

		initLogging("");

		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowListener());

		// ---------- Top panel with absolute layout ----------
		JToolBar top = new JToolBar(null);
		top.setFloatable(false);

		JLabel4j_std lblStart = new JLabel4j_std("Base folder:");
		top.add(lblStart);
		
		top.addSeparator(separator);

		pathField.setSize(new Dimension(360, 24));
		pathField.setMinimumSize(new Dimension(360, 24));
		pathField.setPreferredSize(new Dimension(360, 24));
		top.add(pathField);
		
		JButton4j browseBtn = new JButton4j();
		browseBtn.setPreferredSize(btn);
		browseBtn.setToolTipText("Select Folder");
		top.add(browseBtn);
		browseBtn.setIcon(Common.icon_select_folder);
		
		JButton4j refreshBtn = new JButton4j();
		refreshBtn.setPreferredSize(btn);
		refreshBtn.setToolTipText("Refresh");
		top.add(refreshBtn);
		refreshBtn.setIcon(Common.icon_reload);
		refreshBtn.addActionListener(e -> generate());
		
		top.addSeparator(separator);

		JLabel4j_std lblDepth = new JLabel4j_std("Max depth:");
		top.add(lblDepth);
		
		top.addSeparator(separator);

		depthSpinner.setPreferredSize(new Dimension(70, 24));
		top.add(depthSpinner);
		depthSpinner.revalidate();
		
		top.addSeparator(separator);
		JLabel4j_std lblEntries = new JLabel4j_std("Max entries:");

		top.add(lblEntries);
		top.addSeparator(separator);

		top.add(entriesSpinner);
		entriesSpinner.setPreferredSize(new Dimension(70, 24));
		entriesSpinner.revalidate();
		
		top.addSeparator(separator);
		
		includeFilesChk.setSelected(false);
		top.add(includeFilesChk);
		
		outputArea.setForeground(new Color(255, 0, 0));

		// ---------- Output area ----------
		outputArea.setEditable(false);
		outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		outputArea.setLineWrap(false);
		JScrollPane scroll = new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// ---------- Layout ----------
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(top, BorderLayout.NORTH);
		getContentPane().add(scroll, BorderLayout.CENTER);

		// Defaults
		pathField.setText(defaultStartPath());

		JToolBar right = new JToolBar();
		right.setFloatable(false);
		right.setOrientation(SwingConstants.VERTICAL);
		right.setAlignmentX(SwingConstants.VERTICAL);
		getContentPane().add(right, BorderLayout.EAST);
		
		statusMessage.setFont(Common.font_bold);
		statusMessage.setLocation(3, 6);
		statusMessage.setForeground(new Color(0, 0, 255));
		statusMessage.setSize(new Dimension(22, 13));
		statusMessage.setPreferredSize(new Dimension(800,27));
		statusMessage.setHorizontalAlignment(SwingConstants.LEFT);
		statusBar.setFloatable(false);
		
		getContentPane().add(statusBar,BorderLayout.SOUTH);
		statusBar.addSeparator(separator);
		statusBar.add(statusMessage);


		JButton4j saveBtn = new JButton4j();
		saveBtn.setPreferredSize(btn);
		saveBtn.setToolTipText("Save");
		right.add(saveBtn);
		saveBtn.setIcon(Common.icon_save);
		saveBtn.addActionListener(new ActionListener()
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

						writer.write(outputArea.getText());
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



		JButton4j copyBtn = new JButton4j();
		copyBtn.setPreferredSize(btn);
		copyBtn.setToolTipText("Clipboard");
		right.add(copyBtn);
		copyBtn.setIcon(Common.icon_duplicate);

		copyBtn.addActionListener(e -> copyToClipboard());

		JButton4j clearBtn = new JButton4j();
		clearBtn.setToolTipText("Clear");
		clearBtn.setPreferredSize(btn);
		right.add(clearBtn);
		clearBtn.setIcon(Common.icon_erase);
		
		
		JButton4j btnAbout = new JButton4j(Common.icon_about);
		btnAbout.setToolTipText("About");
		btnAbout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogAbout about = new JDialogAbout();
				about.setVisible(true);
			}
		});
		right.add(btnAbout);

		JButton4j btnLicense = new JButton4j(Common.icon_license);
		btnLicense.setToolTipText("Licences");
		btnLicense.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogLicenses dl = new JDialogLicenses(TreeDoc.this);
				dl.setVisible(true);
			}
		});
		right.add(btnLicense);

		JButton4j btnHelp = new JButton4j(Common.icon_help);
		btnHelp.setToolTipText("Help");

		final JHelp help = new JHelp();
		help.enableHelpOnButton(btnHelp, "https://wiki.commander4j.com");
		right.add(btnHelp);		
		
		
		JButton4j closeBtn = new JButton4j();
		closeBtn.setToolTipText("Close");
		right.add(closeBtn);
		closeBtn.setIcon(Common.icon_exit);
		closeBtn.setPreferredSize(btn);
		closeBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (confirmExit())
				{
					saveConfig();

					System.exit(0);
				}
			}
		});
		clearBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				outputArea.setText("");
			}
		});

		// ---------- Actions ----------
		browseBtn.addActionListener(e -> chooseFolder());
		pack();
		setMinimumSize(new Dimension(900, 650));


		// Generate initial tree
		loadConfig();

		setLocationRelativeTo(null);

		int widthadjustment = util.getOSWidthAdjustment();
		int heightadjustment = util.getOSHeightAdjustment();

		GraphicsDevice gd = util.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - TreeDoc.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - TreeDoc.this.getHeight()) / 2), TreeDoc.this.getWidth() + widthadjustment,
				TreeDoc.this.getHeight() + heightadjustment);
		
		//SwingUtilities.invokeLater(this::generate);
		
		setVisible(true);
		

	}

	private void chooseFolder()
	{
		JFileChooser fc = new JFileChooser(pathField.getText().trim().isEmpty() ? defaultStartPath() : pathField.getText().trim());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File sel = fc.getSelectedFile();
			if (sel != null)
			{
				pathField.setText(sel.getAbsolutePath());
				generate();
			}
		}
	}

	private void copyToClipboard()
	{
		String text = outputArea.getText();
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
		JOptionPane.showMessageDialog(this, "Tree copied to clipboard.", "Copied", JOptionPane.INFORMATION_MESSAGE);
	}

	private void generate()
	{
		String startPath = pathField.getText().trim();
		if (startPath.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Please select a start folder.", "No folder", JOptionPane.WARNING_MESSAGE);
			return;
		}
		File root = new File(startPath);
		if (!root.exists() || !root.isDirectory())
		{
			JOptionPane.showMessageDialog(this, "Invalid folder: " + startPath, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		boolean includeFiles = includeFilesChk.isSelected();
		int maxDepth = ((Number) depthSpinner.getValue()).intValue();
		int maxEntries = ((Number) entriesSpinner.getValue()).intValue();

		StringBuilder sb = new StringBuilder(8192);
		Set<Path> visitedRealPaths = new HashSet<>();
		Counter counter = new Counter();

		try
		{
			sb.append(root.getName()).append(System.lineSeparator());
			Path realRoot = safeRealPath(root.toPath());
			if (realRoot != null)
				visitedRealPaths.add(realRoot);

			List<File> children = filteredChildren(root, includeFiles);
			for (int i = 0; i < children.size(); i++)
			{
				boolean isLast = (i == children.size() - 1);
				String branch = isLast ? "└── " : "├── ";
				buildTree(sb, children.get(i), includeFiles, "", branch, isLast, 1, maxDepth, visitedRealPaths, counter, maxEntries);
				if (counter.value >= maxEntries)
					break;
			}
		}
		catch (Exception ex)
		{
			sb.append("\n[Error] ").append(ex.getMessage()).append('\n');
		}

		outputArea.setText(sb.toString());
		outputArea.setCaretPosition(0);
		
		String msg = "Entries Printed "+String.valueOf(counter.value);
		
		if (counter.value >= maxEntries)
			msg = msg + " (truncated)";
		
		 statusMessage.setText(msg);
	}

	// ---------- Core traversal ----------
	private static void buildTree(StringBuilder sb, File file, boolean includeFiles, String ancestorPrefix, String currentPrefix, boolean isLastSibling, int depth, int maxDepth, Set<Path> visitedRealPaths, Counter counter, int maxEntries)
	{

		if (counter.value >= maxEntries)
			return;

		String linePrefix = ancestorPrefix + currentPrefix;
		sb.append(linePrefix).append(file.getName()).append(System.lineSeparator());
		counter.value++;
		if (counter.value >= maxEntries)
			return;

		if (depth >= maxDepth)
			return;
		if (!file.isDirectory())
			return;

		Path real = safeRealPath(file.toPath());
		if (real != null && !visitedRealPaths.add(real))
			return;

		List<File> children = filteredChildren(file, includeFiles);
		if (children.isEmpty())
			return;

		String nextAncestorPrefix = ancestorPrefix + (isLastSibling ? "    " : "│   ");
		for (int i = 0; i < children.size(); i++)
		{
			boolean last = (i == children.size() - 1);
			String branch = last ? "└── " : "├── ";
			buildTree(sb, children.get(i), includeFiles, nextAncestorPrefix, branch, last, depth + 1, maxDepth, visitedRealPaths, counter, maxEntries);
			if (counter.value >= maxEntries)
				break;
		}
	}

	private static List<File> filteredChildren(File dir, boolean includeFiles)
	{
		File[] arr = dir.listFiles();
		if (arr == null)
			return Collections.emptyList();

		List<File> dirs = new ArrayList<>();
		List<File> files = new ArrayList<>();

		for (File f : arr)
		{
			if (!shouldInclude(f, includeFiles))
				continue;
			if (f.isDirectory())
				dirs.add(f);
			else
				files.add(f);
		}

		Comparator<File> byName = Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT));
		dirs.sort(byName);
		files.sort(byName);

		List<File> out = new ArrayList<>(dirs.size() + files.size());
		out.addAll(dirs);
		if (includeFiles)
			out.addAll(files);
		return out;
	}

	private static boolean shouldInclude(File f, boolean includeFiles)
	{
		if (f.isHidden())
			return false;
		String name = f.getName();
		if (name.equals(".DS_Store") || name.equals(".localized"))
			return false;
		if (name.toLowerCase(Locale.ROOT).endsWith(".app"))
			return false;
		if (!includeFiles && !f.isDirectory())
			return false;
		try
		{
			if (Files.isSymbolicLink(f.toPath()))
				return false;
		}
		catch (Exception ignored)
		{
		}
		return true;
	}

	private static Path safeRealPath(Path p)
	{
		try
		{
			return p.toRealPath();
		}
		catch (IOException | SecurityException e)
		{
			return null;
		}
	}

	private static class Counter
	{
		int value = 0;
	}

	private static String defaultStartPath()
	{

		return System.getProperty("user.home");
	}

	// ---------- Launch demo ----------
	public static void main(String[] args)
	{

		JUtility util = new JUtility();

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		util.setLookAndFeel("Nimbus");

		TreeDoc treeDoc = new TreeDoc();
		treeDoc.setTitle("TreeDoc v" + version);
		treeDoc.setSize(900, 750);

		GraphicsDevice gd = util.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		treeDoc.setBounds(screenBounds.x + ((screenBounds.width - treeDoc.getWidth()) / 2), screenBounds.y + ((screenBounds.height - treeDoc.getHeight()) / 2), treeDoc.getWidth(), treeDoc.getHeight());

		treeDoc.setIconImage(treeDoc.logo.getImage());

		treeDoc.setVisible(true);

	}

	private void loadConfig()
	{

		JXMLDocument xmlMessage = new JXMLDocument();
		String rootFolder = System.getProperty("user.dir");
		String includeFilesStr = "N";

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

		pathField.setText(rootFolder);

		if (includeFilesStr.isBlank())
		{
			includeFilesStr = "true";
		}

		includeFilesChk.setSelected(Boolean.valueOf(includeFilesStr));

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
			Text text = document.createTextNode(pathField.getText());
			rootFolder.appendChild(text);
			TreeDoc.appendChild(rootFolder);

			Element includeFilesStr = (Element) document.createElement("includeFiles");
			text = document.createTextNode(String.valueOf(includeFilesChk.isSelected()));

			includeFilesStr.appendChild(text);
			TreeDoc.appendChild(includeFilesStr);

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

	public void initLogging(String filename)
	{
		if (filename.isEmpty())
		{
			filename = System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "log4j2.xml";
		}

		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		File file = new File(filename);

		context.setConfigLocation(file.toURI());

		if (factory instanceof Log4jContextFactory)
		{
			// LOG.info("register shutdown hook");
			Log4jContextFactory contextFactory = (Log4jContextFactory) factory;

			((DefaultShutdownCallbackRegistry) contextFactory.getShutdownCallbackRegistry()).stop();
		}

	}

	public boolean confirmExit()
	{
		boolean result = false;

		int question = JOptionPane.showConfirmDialog(TreeDoc.this, "Exit application ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, null);

		if (question == 0)
		{
			result = true;
		}
		return result;
	}

	class WindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			if (confirmExit())
			{
				saveConfig();

				System.exit(0);
			}
		}

	}
}
