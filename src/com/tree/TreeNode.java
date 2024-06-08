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

	public static TreeNode<File> createDirTree(File folder)
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
							}
						}
						else
						{
							if (Files.isRegularFile(path))
							{
								fileType = "Regular File";
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

					if (fileType.equals("Directory"))
					{

						appendDirTree(file, DirRoot);

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

		return (Files.exists(contentsPath) && Files.exists(infoPlistPath)) || Files.exists(wrapperPath)|| Files.exists(parallesPath);
	}

	public static void appendDirTree(File folder, TreeNode<File> DirRoot)
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
					if (file.isDirectory())
					{
						Path p = file.toPath();
						if (isAppBundle(p) == false)
						{
							try
							{
								if (Files.isHidden(p) == false)
								{
									appendDirTree(file, DirRoot.children.get(DirRoot.children.size() - 1));
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

	public static String displayTree(File file)
	{
		TreeNode<File> DirTree = createDirTree(file);
		String result = renderDirectoryTree(DirTree);
		return result;
	}

}