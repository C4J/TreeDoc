package com.tree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeNode<T> implements Iterable<TreeNode<T>>
{

	public T data;
	public TreeNode<T> parent;
	public List<TreeNode<T>> children;

	public boolean isRoot()
	{
		return parent == null;
	}

	public boolean isLeaf()
	{
		return children.size() == 0;
	}

	private List<TreeNode<T>> elementsIndex;

	public TreeNode(T data)
	{
		this.data = data;
		this.children = new LinkedList<TreeNode<T>>();
		this.elementsIndex = new LinkedList<TreeNode<T>>();
		this.elementsIndex.add(this);
	}

	public TreeNode<T> addChild(T child)
	{
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		this.registerChildForSearch(childNode);
		return childNode;
	}

	public int getLevel()
	{
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	private void registerChildForSearch(TreeNode<T> node)
	{
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}

	public TreeNode<T> findTreeNode(Comparable<T> cmp)
	{
		for (TreeNode<T> element : this.elementsIndex)
		{
			T elData = element.data;
			if (cmp.compareTo(elData) == 0)
				return element;
		}

		return null;
	}

	@Override
	public String toString()
	{
		return data != null ? data.toString() : "[data null]";
	}

	@Override
	public Iterator<TreeNode<T>> iterator()
	{
		TreeNodeIter<T> iter = new TreeNodeIter<T>(this);
		return iter;
	}

	public static TreeNode<File> createDirTree(File folder, boolean includeFiles, String filter, LinkedList<String> excludeFolders, LinkedList<String> excludeFiles)
	{
		if (!folder.isDirectory())
		{
			throw new IllegalArgumentException("folder is not a Directory");
		}

		TreeNode<File> DirRoot = new TreeNode<File>(folder);

		File filenames[] = folder.listFiles();

		if (filenames != null)
		{

			if (filenames.length > 0)
			{

				Arrays.sort(filenames);

				for (File file : filenames)
				{
					String fileType = "";

					Path path = Paths.get(file.getAbsolutePath());
					//System.out.println(">>>>>>"+path.getFileName());

					try
					{
						// Check if the path is a directory
						if (Files.isDirectory(path))
						{
							if (isAppBundle(path))
							{
								fileType = "App Bundle";
							}
							else
							{
								fileType = "Directory";
								
								if (excludeFolders.size() > 0)
								{
									for (int x=0;x<excludeFolders.size();x++)
									{
										//System.out.println(path.getFileName().toString()+" - "+excludeFolders.get(x));
										if (path.getFileName().toString().endsWith(excludeFolders.get(x)))
										{
											fileType = "Ignore Folder";
											
											break;
										}
									}
								}
							}
						}
						else
						{
							if (Files.isRegularFile(path))
							{
								fileType = "Regular File";
								
								if (excludeFiles.size() > 0)
								{
									for (int x=0;x<excludeFiles.size();x++)
									{
										//System.out.println(path.getFileName().toString()+" - "+excludeFiles.get(x));
										if (path.getFileName().toString().endsWith(excludeFiles.get(x)))
										{
											fileType = "Ignore File";
											
											break;
										}
									}
								}
								//System.out.println(fileType);
								
							}
							else
							{
								if (Files.isSymbolicLink(path))
								{
									fileType = "Symbolic Link";
								}
								else
								{
									fileType = "Unknown";
								}
							}
						}

						if (Files.isHidden(path))
						{
							fileType = "Hidden";
						}

					}
					catch (IOException e)
					{
						fileType = e.getMessage();
					}

					if (fileType.equals("Directory") || fileType.equals("Regular File"))
					{

						appendDirTree(file, DirRoot, includeFiles, filter, excludeFolders,excludeFiles);

					}
				}
			}
		}
		return DirRoot;
	}

	private static boolean isAppBundle(Path path)
	{

		Path contentsPath = path.resolve("Contents");

		Path infoPlistPath = contentsPath.resolve("Info.plist");

		Path wrapperPath = path.resolve("Wrapper");

		Path parallesPath = path.resolve("config.pvs");

		return (Files.exists(contentsPath) && Files.exists(infoPlistPath)) || Files.exists(wrapperPath) || Files.exists(parallesPath);
	}

	public static void appendDirTree(File folder, TreeNode<File> DirRoot, boolean includeFiles, String filter, LinkedList<String> excludeFolders, LinkedList<String> excludeFiles)
	{
		DirRoot.addChild(folder);

		File filenames[] = folder.listFiles();

		if (filenames != null)
		{

			if (filenames.length > 0)
			{

				Arrays.sort(filenames);

				for (File file : filenames)
				{
					if (file.isDirectory() || (file.isFile() && includeFiles))
					{
						Path p = file.toPath();
						if (isAppBundle(p) == false)
						{
							try
							{
								if (Files.isHidden(p) == false)
								{
									boolean fileValid = true;
									
									if (file.isFile())
									{
										if (excludeFiles.size()>0)
										{
											for (int x=0;x<excludeFiles.size();x++)
											{
												if (file.getName().endsWith(excludeFiles.get(x)))
												{
													fileValid = false;
												}
											}
										}
									}
									
									boolean folderValid = true;
									
									if (file.isDirectory())
									{
										if (excludeFolders.size()>0)
										{
											for (int x=0;x<excludeFolders.size();x++)
											{
												if (file.getName().endsWith(excludeFolders.get(x)))
												{
													folderValid = false;
												}
											}									
										}
									}
									
									if ((folderValid) && (fileValid))
									//if ((file.isDirectory() && (excludeFolders.contains(p.getFileName().toString())==false)) || (file.isFile() && file.getAbsoluteFile().toString().toLowerCase().contains(filter.toLowerCase()))  && (excludeFiles.contains(p.getFileName().toString())==false))
									{

									//	if (excludes.contains(p.getFileName().toString()))
										{
											appendDirTree(file, DirRoot.children.get(DirRoot.children.size() - 1), includeFiles, filter, excludeFolders,excludeFiles);
										}

									}
								}
							}
							catch (IOException e)
							{

							}
						}
					}

				}
			}
		}
	}

	public static void appendFile(File file, TreeNode<File> filenode)
	{
		filenode.addChild(file);
	}

	public static String renderDirectoryTree(TreeNode<File> tree)
	{
		List<StringBuilder> lines = renderDirectoryTreeLines(tree);
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder(lines.size() * 20);
		for (StringBuilder line : lines)
		{
			sb.append(line);
			sb.append(newline);
		}
		return sb.toString();
	}

	public static List<StringBuilder> renderDirectoryTreeLines(TreeNode<File> tree)
	{
		List<StringBuilder> result = new LinkedList<>();
		result.add(new StringBuilder().append(tree.data.getName()));
		Iterator<TreeNode<File>> iterator = tree.children.iterator();
		while (iterator.hasNext())
		{
			List<StringBuilder> subtree = renderDirectoryTreeLines(iterator.next());
			if (iterator.hasNext())
			{
				addSubtree(result, subtree);
			}
			else
			{
				addLastSubtree(result, subtree);
			}
		}
		return result;
	}

	private static void addSubtree(List<StringBuilder> result, List<StringBuilder> subtree)
	{
		Iterator<StringBuilder> iterator = subtree.iterator();
		// subtree generated by renderDirectoryTreeLines has at least one line
		// which is tree.getData()
		result.add(iterator.next().insert(0, "├── "));
		while (iterator.hasNext())
		{
			result.add(iterator.next().insert(0, "│   "));
		}
	}

	private static void addLastSubtree(List<StringBuilder> result, List<StringBuilder> subtree)
	{
		Iterator<StringBuilder> iterator = subtree.iterator();
		// subtree generated by renderDirectoryTreeLines has at least one line
		// which is tree.getData()
		result.add(iterator.next().insert(0, "└── "));
		while (iterator.hasNext())
		{
			result.add(iterator.next().insert(0, "    "));
		}
	}

	public static String displayTree(File file, boolean includeFiles, String filter, String textFolderExcludes,String textFileExcludes)
	{
		LinkedList<String> excludedFolders = new LinkedList<String>();

		String[] temp = textFolderExcludes.split("\n");

		for (int x = 0; x < temp.length; x++)
		{
			if (temp[x].equals("") == false)
			{
				excludedFolders.add(temp[x]);
			}
		}
		
		LinkedList<String> excludedFiles = new LinkedList<String>();

		temp = textFileExcludes.split("\n");

		for (int x = 0; x < temp.length; x++)
		{
			if (temp[x].equals("") == false)
			{
				excludedFiles.add(temp[x]);
			}
		}

		TreeNode<File> DirTree = createDirTree(file, includeFiles, filter, excludedFolders,excludedFiles);
		String result = renderDirectoryTree(DirTree);
		return result;
	}

}