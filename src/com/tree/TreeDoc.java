package com.tree;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class TreeDoc extends JFrame
{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel textField;
	private Font defaultFont = new Font("Courier New", Font.PLAIN, 12);
	private Font boldFont = new Font("Courier New", Font.BOLD, 12);

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
		setTitle("Tree Documentor");
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

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 77, 878, 457);
		desktopPane.add(scrollPane);

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(new Color(255, 0, 0));
		textArea.setFont(defaultFont);
		textArea.setBackground(SystemColor.window);
		textArea.setBounds(0, 0, 7, 21);
		textArea.setBorder(BorderFactory.createLineBorder(Color.black));
		scrollPane.setViewportView(textArea);

		JButton btnBrowse = new JButton("Browse for Folder");
		btnBrowse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int option = fileChooser.showOpenDialog(TreeDoc.this);
				
				if (option == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					textField.setText(file.getAbsolutePath());
					textArea.setText(TreeNode.displayTree(file));
					textArea.setCaretPosition(0);
				}

			}
		});
		btnBrowse.setFont(boldFont);
		btnBrowse.setBounds(12, 10, 175, 25);
		desktopPane.add(btnBrowse);

		textField = new JLabel();
		textField.setForeground(new Color(0, 0, 255));
		textField.setFont(defaultFont);
		textField.setBounds(12, 45, 878, 25);
		textField.setOpaque(true);
		textField.setBackground(SystemColor.window);
		textField.setBorder(BorderFactory.createLineBorder(Color.black));
		desktopPane.add(textField);

		JButton btnSave = new JButton("Save Tree to File");
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

						FileWriter fw = new FileWriter(newTextFile);
						fw.write(textArea.getText());
						fw.close();

					}
					catch (IOException iox)
					{
						iox.printStackTrace();
					}
				}

			}
		});
		btnSave.setFont(boldFont);
		btnSave.setBounds(199, 10, 175, 25);
		desktopPane.add(btnSave);
		
		JButton btnQuit = new JButton("Quit");
		btnQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnQuit.setFont(boldFont);
		btnQuit.setBounds(573, 10, 175, 25);
		desktopPane.add(btnQuit);
		
		JButton btnClearOutput = new JButton("Clear Output");
		btnClearOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});
		btnClearOutput.setFont(boldFont);
		btnClearOutput.setBounds(386, 10, 175, 25);
		desktopPane.add(btnClearOutput);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(0, 0, 3, 3);
		desktopPane.add(scrollPane_1);
	}
}
