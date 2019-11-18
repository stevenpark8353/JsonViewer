package hk.hkk.jsonViewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.miginfocom.swing.MigLayout;
import javax.swing.JCheckBox;

/**
 * 
 * @author Bana
 * @author stevenpark8353@gmail.com
 * @version 1.0.0
 *
 */
public class JSONViewer extends JFrame {
	static class TreeIcon implements Icon {

		private static int SIZE = 0;

		public TreeIcon() {
		}

		public int getIconWidth() {
			return SIZE;
		}

		public int getIconHeight() {
			return SIZE;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
		}
	}

	static {
		// try {
		// Font font = Font.createFont(Font.TRUETYPE_FONT,
		// JSONEditor.class.getResource("NANUMGOTHIC.TTF").openStream());
		// GraphicsEnvironment ge =
		// GraphicsEnvironment.getLocalGraphicsEnvironment();
		// ge.registerFont(font);
		//
		// } catch (IOException | FontFormatException e) {
		// }
		setUIFont(new javax.swing.plaf.FontUIResource("나눔고딕", Font.PLAIN, 13));

		Icon empty = new TreeIcon();
		// UIManager.put("Tree.collapsedIcon", empty);
		// UIManager.put("Tree.expandedIcon", empty);
		UIManager.put("Tree.leafIcon", empty);
		UIManager.put("Tree.openIcon", empty);
		UIManager.put("Tree.closedIcon", empty);
		// UIManager.put("Tree.leafIcon", new
		// ImageIcon(JSONEditor.class.getResource("tree_dir.png")));

		UIManager.put("Tree.expandedIcon", new ImageIcon(JSONViewer.class.getResource("tree_expanded.png")));
		UIManager.put("Tree.collapsedIcon", new ImageIcon(JSONViewer.class.getResource("tree_collapsed.png")));

		// UIManager.put("Tree.openIcon", new
		// ImageIcon(JSONEditor.class.getResource("tree_expanded.png")));
		// UIManager.put("Tree.closedIcon", new
		// ImageIcon(JSONEditor.class.getResource("tree_collapsed.png")));
	}

	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}

	private static final long serialVersionUID = 8757981281205336488L;
	final int width = 800;
	final int height = 600;
	private JTree tree;
	private JButton btnExpandAll;
	private JTextField tfKey;
	private JTextField textField;
	private JCheckBox chkAutoExpand;

	public JSONViewer() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(width, height));
		getContentPane().setLayout(new MigLayout("hidemode 3", "[grow][]", "[][grow][]"));

		JPanel panelTop = new JPanel();
		getContentPane().add(panelTop, "cell 0 0 2 1,grow");
		panelTop.setLayout(new MigLayout("insets 5 0 5 0", "[97px][]", "[23px]"));

		btnExpandAll = new JButton("Expand All");
		btnExpandAll.setEnabled(false);
		btnExpandAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeNode root = (TreeNode) tree.getModel().getRoot();
				expandAll(tree, new TreePath(root));
			}
		});
		panelTop.add(btnExpandAll, "cell 0 0,alignx left");
		
		chkAutoExpand = new JCheckBox("Auto Expand");
		panelTop.add(chkAutoExpand, "cell 1 0,alignx left");

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 1,grow");
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				if ((e.getID() == KeyEvent.KEY_PRESSED) && (e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					String pasted = null;
					Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
					try {
						pasted = c.getData(DataFlavor.stringFlavor).toString();
					} catch (UnsupportedFlavorException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					if (pasted != null) {
						refresh0(scrollPane, "", pasted);
					}
				}
				return false;
			}
		});

		new FileDrop(scrollPane, /* dragBorder, */ new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				try {
					refresh(scrollPane, files[0].getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} // end filesDropped
		}); // end FileDrop.Listener

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVisible(false);
		getContentPane().add(scrollPane_1, "cell 1 1,grow");

		JPanel panelRight = new JPanel();
		scrollPane_1.setViewportView(panelRight);
		panelRight.setLayout(new MigLayout("", "[grow]", "[][][grow]"));

		JPanel panelKey = new JPanel();
		panelRight.add(panelKey, "cell 0 0,grow");
		panelKey.setLayout(new MigLayout("", "[][grow]", "[]"));

		JLabel lblNewLabel_1 = new JLabel("Key : ");
		panelKey.add(lblNewLabel_1, "cell 0 0,alignx left");

		tfKey = new JTextField();
		panelKey.add(tfKey, "cell 1 0,growx");
		tfKey.setColumns(10);

		JPanel panelValue0 = new JPanel();
		panelRight.add(panelValue0, "cell 0 1,grow");
		panelValue0.setLayout(new MigLayout("", "[][grow]", "[]"));

		JLabel lblNewLabel_3 = new JLabel("Value : ");
		panelValue0.add(lblNewLabel_3, "cell 0 0,alignx left");

		textField = new JTextField();
		panelValue0.add(textField, "cell 1 0,growx");

		JPanel panelValue1 = new JPanel();
		panelRight.add(panelValue1, "cell 0 2,grow");
		panelValue1.setLayout(new MigLayout("", "[grow]", "[][grow]"));

		JLabel lblNewLabel_2 = new JLabel("Value : ");
		panelValue1.add(lblNewLabel_2, "cell 0 0");

		JScrollPane scrollPane_2 = new JScrollPane();
		panelValue1.add(scrollPane_2, "cell 0 1,grow");

		JPanel panel_1 = new JPanel();
		scrollPane_2.setViewportView(panel_1);
		panel_1.setLayout(new MigLayout("", "[grow]", "[grow][]"));

		JTree tree_1 = new JTree();
		panel_1.add(tree_1, "cell 0 0,grow");

		JButton btnNewButton = new JButton("New button");
		panel_1.add(btnNewButton, "cell 0 1");

		JPanel panel = new JPanel();
		getContentPane().add(panel, "cell 0 2 2 1,alignx right,growy");

		JLabel lblNewLabel = new JLabel("Made By Bana");
		panel.add(lblNewLabel);
	}

	private void expandAll(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path);
			}
		}
		tree.expandPath(parent);
	}
	
	void refresh0(JScrollPane scrPane, String title, String json) {
		Object jsonObj = null;
		if ((jsonObj = isValidJson(json)) == null) {
			JOptionPane.showMessageDialog(this, "Invalid JSON", "ALART", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (tree == null) {
			tree = new JTree();
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					TreeObject treeObj = (TreeObject) node.getUserObject();
					if (treeObj != null) {
						tfKey.setText(treeObj.key);
					}
					// e.getPath()
					// tfKey.setText(e.toString());
				}
			});
			tree.setEditable(false);
			scrPane.setViewportView(tree);
		} else {
			tree.removeAll();
		}

		addBranches(jsonObj);
		this.setTitle("JSON Viewer v0.1 - " + title);
		btnExpandAll.setEnabled(true);
		if (chkAutoExpand.isSelected()) {
			TreeNode root = (TreeNode) tree.getModel().getRoot();
			expandAll(tree, new TreePath(root));
		}

		return;
	}

	void refresh(JScrollPane scrPane, String filePath) {
		refresh0(scrPane, filePath, loadStringFromFile(filePath));
	}

	Object isValidJson(String json) {
		Object obj = null;
		if (json == null) {
			return null;
		}
		JSONParser jsonParser = new JSONParser();
		try {
			obj = jsonParser.parse(json);
		} catch (ParseException e) {
//			e.printStackTrace();
			return null;
		}

		return obj;
	}

	String loadStringFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}

		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		}

		return sb.toString();
	}

	boolean addBranches(Object jsonObj) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeObject("", "", ""));
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		treeModel.setRoot(rootNode);
		insertIntoTree(jsonObj, rootNode, 1);
		tree.expandRow(0);
		return true;
	}

	void insertIntoTree(Object obj, DefaultMutableTreeNode parent, int depth) {
		if (obj instanceof JSONObject) {
			JSONObject json = (JSONObject) obj;
			for (Object _key : json.keySet()) {
				String key = (String) _key;
				Object value = json.get(key);
				DefaultMutableTreeNode node = addToNode(key, value);
				parent.add(node);
				insertIntoTree(value, node, depth + 1);
			}
		} else if (obj instanceof JSONArray) {
			JSONArray jsonArr = (JSONArray) obj;
			for (Object jsonObj : jsonArr) {
				DefaultMutableTreeNode node = addToNode("", jsonObj);
				parent.add(node);
				insertIntoTree(jsonObj, node, depth + 1);
			}
		} else {
			return;
		}

	}

	DefaultMutableTreeNode addToNode(String key, Object value) {
		String str = null;
		if (key.length() > 0) {
			str = "\"" + key + "\" : ";
		} else {
			str = "";
		}
		if (value instanceof JSONObject) {
			str += "(+)";
		} else if (value instanceof JSONArray) {
			str += "[ ]";
		} else {
			if (value instanceof String) {
				str += "\"" + value + "\"";
			} else {
				str += value;
			}
		}

		TreeObject treeObj = new TreeObject(str, key, value);
		return new DefaultMutableTreeNode(treeObj);
	}

	void showAndDisplay() {
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
