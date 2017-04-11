import javax.swing.*;
import javax.swing.SwingWorker;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;


/*/////////////////////////////////////
Luis Cortes
CS 380
Project 1 
/////////////////////////////////////*/
public class ChatClient extends JFrame {
	private JPanel panel; 
	private JPanel fieldAndButtonPanel;
	private JLabel nameLabel;
	private JTextArea textArea;
	private JTextField textField;
	private JButton sendButton;

	private String ip; 
	private String userText; // Will not be null when user has entered a value
							 // in the textfield and press send
	private PrintStream outStream;

	private final int WINDOW_WIDTH = 600;
	private final int WINDOW_HEIGHT = 400;
	private final int PORT = 38001;


	public ChatClient() {
		initComp();
		getIP();

		System.out.println("Establishing connection ...");
		textArea.append("Establishing connection\n");

		try {
			Socket socket = new Socket(ip, 38001);
			System.out.println("conncted");
			addToAreaText("Connected to "+ip);
			addToAreaText("");

			sendButton.setEnabled(true);

			// To print out to the server
			outStream = new PrintStream(
					socket.getOutputStream(), true, "UTF-8");
			
			// To read from the server
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			ListenToServer listener = new ListenToServer(this.textArea,
					new BufferedReader(isr));
			listener.execute();

		} catch (IOException e) {e.printStackTrace();} 

	}

	/**
	 *	Use URL to get the ip address. 
	 */
	private void getIP() {
		try {
			InetAddress address = InetAddress.getByName(
				new URL("http://codebank.xyz").getHost());
			ip = address.getHostAddress();
		} catch (Exception e) { }
	}

	/**
	 *	All swing components are inintialized and configured	
	 */
	private void initComp() {

		// Set up Frame
		this.setTitle("Chat Client");
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		// Panel
		panel = new JPanel();
		this.add(panel);

		// JText Area, where messages will appear
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setRows(16);
		textArea.setColumns(40);

		// Scroll pane for JTextArea
		JScrollPane scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane);

		// Pane for text field and send button
		fieldAndButtonPanel = new JPanel();
		fieldAndButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15,0));
		this.add(fieldAndButtonPanel);

		// JText field to enter text
		textField = new JTextField(30);
		textField.setPreferredSize(new Dimension(10,60));
		fieldAndButtonPanel.add(textField);

		// JButton to send info
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		fieldAndButtonPanel.add(sendButton);
		sendButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {
				SendButtonActionPerforemd(event); 
			}
		});
	}

	/**
	 *	Retreive text from textField, erase the text field, and 
	 *  send text to user
	 */
	private void SendButtonActionPerforemd(ActionEvent event) {
		System.out.println("send button pressed");
		// String text = textField.getText();
		userText = new String(textField.getText());
		textField.setText("");
		outStream.println(userText); // Send to server
		textArea.append("ME < "+userText.toString());
		textArea.append("\n");
		System.out.println("**"+userText+"**");
	}

	/**
	 *	Show text in GUI
	 */
	private void addToAreaText(String info) {
		textArea.append(info+"\n");
	}

	private void clearTextArea() {
		textArea.setText("");
	}

	///*** SERVER LISTENER CLASS *** ///
	///							   ///
	/**
	 *	Class (Thread) listens for server input and displays it to Gui 
	 */
	private class ListenToServer extends SwingWorker<Void, String> {
		final private JTextArea messageArea; 
		final private BufferedReader bufferReader;

		public ListenToServer(final JTextArea messageArea, 
			final BufferedReader bufferReader) {
			this.messageArea = messageArea;
			this.bufferReader = bufferReader;
		} 

		@Override
		protected Void doInBackground() {
			System.out.println("Listening for server ... ");
			while(true) {
				try {
					String word = bufferReader.readLine();
					System.out.println(word);
					if (word != null) {
						publish(word);
					} 

					if (!bufferReader.ready()) return null;
				} catch (IOException e) {e.printStackTrace();}
			} // end while
		}

		@Override
		protected void process(List<String> chunks) {
			for (final String string : chunks) {
				messageArea.append("Server < "+string);
				messageArea.append("\n");
			}
		}
	}
	//									///
	/// *** End of Server Listener Class *** ///

	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new ChatClient().setVisible(true);
			}
		});
	}
}