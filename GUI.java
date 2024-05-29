package main.gui;

import main.game.*;
import main.game.Settings;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.*;

public class GUI extends JFrame {

	private Game game;
	private ArrayList<BoardState> possibleMoves;
	private Square[] squares;
	private JPanel checkerboardPanel;
	private JPanel contentPane;
	private JTextArea textBox;
	private BoardState hintMove;

	private List<Integer> helpMoves;
	public GUI() {
		start();
	}

	private void start() {

		settingsPopup();
		game = new Game(this); //initial game
		possibleMoves = new ArrayList<>();
		hintMove = null;
	}

	
	//
	private void settingsPopup() {
		
		JPanel panel = new JPanel(new GridLayout(8, 1));

		JLabel text1 = new JLabel("Thiết lập chế độ chơi", 10);

		JRadioButton forceTakesButton = new JRadioButton("Force Takes");
		
		forceTakesButton.setSelected(Settings.FORCETAKES);

		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton humanFirstRadioButton = new JRadioButton("Bạn chơi trước");
		JRadioButton aiRadioButton = new JRadioButton("Máy chơi trước");
		buttonGroup.add(humanFirstRadioButton);
		buttonGroup.add(aiRadioButton);
		aiRadioButton.setSelected(Settings.FIRSTMOVE == Player.AI);
		humanFirstRadioButton.setSelected(Settings.FIRSTMOVE == Player.HUMAN);
		
		panel.add(text1);

		panel.add(forceTakesButton);
		panel.add(humanFirstRadioButton);
		panel.add(aiRadioButton);

		
		JLabel text2 = new JLabel("Thiết lập độ khó", 10);
		JRadioButton minimaxRadioButton = new JRadioButton("Minimax");
		JRadioButton alphabetaRadioButton = new JRadioButton("Alphabeta");

		ButtonGroup algorithmGroup = new ButtonGroup();
		algorithmGroup.add(minimaxRadioButton);
		algorithmGroup.add(alphabetaRadioButton);

		panel.add(text2);
		panel.add(minimaxRadioButton);
		panel.add(alphabetaRadioButton);

		int result = JOptionPane.showConfirmDialog(null, panel, "Thiết lập trò chơi", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			System.out.println("AI depth = " + Settings.AI_DEPTH);
			Settings.FIRSTMOVE = humanFirstRadioButton.isSelected() ? Player.HUMAN : Player.AI;
			Settings.FORCETAKES = forceTakesButton.isSelected();
			
			if (minimaxRadioButton.isSelected()) {
				System.out.println("Chế độ chơi của người chơi: Minimax");
				Settings.SELECTED_ALGORITHM = "minimax";
			} else if (alphabetaRadioButton.isSelected()) {
				System.out.println("Chế độ chơi của người chơi: Alpha-Beta");
				Settings.SELECTED_ALGORITHM1 = "alphabeta";
			}
		}
	}



	public void setup() {
		
		//
		switch (Settings.FIRSTMOVE) {
		case AI:
			main.gui.Settings.AIcolour = Colour.WHITE;
			break;
		case HUMAN:
			main.gui.Settings.AIcolour = Colour.BLACK;
			break;
		}
	
		setupMenuBar();
		contentPane = new JPanel();
		checkerboardPanel = new JPanel(new GridBagLayout());
		JPanel textPanel = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		this.setContentPane(contentPane);
		contentPane.add(checkerboardPanel);
		contentPane.add(textPanel);
		textBox = new JTextArea();
		textBox.setEditable(false);
		textBox.setLineWrap(false);
		textBox.setWrapStyleWord(true);
		textBox.setAutoscrolls(true);
		textPanel.add(textBox);

		updateCheckerBoard();
		updateText("");
		this.pack();
		this.setVisible(true);

		if (Settings.FIRSTMOVE == Player.AI) {
			aiMove();
		}
	}

	private void updateText(String text) {
		textBox.setText(text);
	}

	private void updateCheckerBoard() {
		checkerboardPanel.removeAll();
		//--Huy
		addPieces();
		addSquares();
		addGhostButtons();
		checkerboardPanel.setVisible(true);
		checkerboardPanel.repaint();
		this.pack();
		this.setVisible(true);
	}
	
	//Chèn Square (Nền) vào bàn cờ --Huy
	private void addSquares() {
		squares = new Square[game.getState().NO_SQUARES];
		int fromPos = -1;
		int toPos = -1;
		if (hintMove != null) {
			fromPos = hintMove.getFromPos();
			toPos = hintMove.getToPos();
		}

		GridBagConstraints c = new GridBagConstraints();
		
		for (int i = 0; i < game.getState().NO_SQUARES; i++) {
			c.gridx = i % game.getState().SIDE_LENGTH;
			c.gridy = i / game.getState().SIDE_LENGTH;
 
			squares[i] = new Square(c.gridx, c.gridy);
			
			if (i == fromPos) {
				squares[i].setHighlighted();
			}
			if (i == toPos) {
				squares[i].setHighlighted();
			}
			if (helpMoves != null) {
				if (helpMoves.contains(i)) {
					squares[i].setHighlighted();
				}
			}

			checkerboardPanel.add(squares[i], c);
		}
	}

	private void addPieces() {
	
		GridBagConstraints c = new GridBagConstraints();
		for (int i = 0; i < game.getState().NO_SQUARES; i++) {

			c.gridx = i % game.getState().SIDE_LENGTH;

			c.gridy = i / game.getState().SIDE_LENGTH;
		
			if (game.getState().getPiece(i) != null) {

				Piece piece = game.getState().getPiece(i);

				if (piece != null) { 
				CheckerButton button = new CheckerButton(i, piece, this);

					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent actionEvent) {
							onPieceClick(actionEvent);
						}
					});

					checkerboardPanel.add(button, c);
				}
			}
		}
	}


	private void addGhostButtons() {

		for (BoardState state : possibleMoves) {

			int newPos = state.getToPos();

			GhostButton button = new GhostButton(state);
			button.addActionListener(new ActionListener() {
				@Override

				public void actionPerformed(ActionEvent actionEvent) {
					onGhostButtonClick(actionEvent);
				}
			});

			squares[newPos].add(button);
		}
	}

	private void setupMenuBar() {

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onExitClick();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Thiết lập");
		JMenuItem restartItem = new JMenuItem("Chơi lại");
		JMenuItem quitItem = new JMenuItem("Thoát");
		JMenu editMenu = new JMenu("Chỉnh sửa");
		JMenuItem undoItem = new JMenuItem("Quay lại");
		JMenu viewMenu = new JMenu("Chế độ");
		JRadioButtonMenuItem viewItemHelpMode = new JRadioButtonMenuItem("Chế độ trợ giúp");
		JRadioButtonMenuItem viewItemHintMode = new JRadioButtonMenuItem("Chế độ gợi ý");
		viewItemHelpMode.setSelected(main.gui.Settings.helpMode);
		viewItemHintMode.setSelected(main.gui.Settings.hintMode);
		JMenu helpMenu = new JMenu("Trợ giúp");
		JMenuItem rulesItem = new JMenuItem("Luật chơi");
		JMenuItem helpItemHint = new JMenuItem("Gợi ý");
		JMenuItem helpItemMovables = new JMenuItem("Hiển thị các quân có thể di chuyển");

		quitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onExitClick();
			}
		});
		restartItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onRestartClick();
			}
		});
		rulesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onRulesClick();
			}
		});
		undoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onUndoClick();
			}
		});
		viewItemHelpMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onHelpModeClick();
			}
		});
		viewItemHintMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onHintModeClick();
			}
		});
		helpItemHint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onHintClick();
			}
		});
		helpItemMovables.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				onHelpMovablesClick();
			}
		});

		fileMenu.add(restartItem);
		fileMenu.add(quitItem);
		editMenu.add(undoItem);
		viewMenu.add(viewItemHelpMode);
		viewMenu.add(viewItemHintMode);
		helpMenu.add(helpItemHint);
		helpMenu.add(helpItemMovables);
		helpMenu.add(rulesItem);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);
		this.setJMenuBar(menuBar);
	}
	
	
	private void onRestartClick() {
		Object[] options = { "Có", "Không", };
		int n = JOptionPane.showOptionDialog(this, "Bạn có chắc là bạn muốn bắt đầu lại không?",
				"Khởi động lại trò chơi? ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[1]);
		if (n == 0) {
			start();
		}
	}

	
	private void onExitClick() {
		Object[] options = { "Có", "Không", };
		int n = JOptionPane.showOptionDialog(this, "\nbạn có chắc bạn muốn thoát?", "Thoát trò chơi? ",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0) {

			this.dispose();
			System.exit(0);
		}
	}

	private void onRulesClick() {

		String message = "1. Chỉ được phép di chuyển trên các ô vuông màu tối, vì vậy các quân cờ luôn di chuyển theo đường chéo. <br /> <br /> " +

				"2. Một quân thực hiện một nước đi không bắt được quân địch chỉ có thể di chuyển một ô vuông. <br /> <br />"
				+

				"3. Một quân thực hiện một động tác bắt (một bước nhảy) nhảy qua một trong các quân của đối phương, hạ cánh theo một đường chéo thẳng ở phía bên kia. Chỉ có thể bắt được một quân trong một lần nhảy; tuy nhiên được phép nhảy nhiều lần trong một lượt. <br /> <br />" +

				"4.Khi một quân cờ bị bắt, nó sẽ bị loại bỏ khỏi bảng. <br /> <br />"
				+ "5. Nếu người chơi có thể bắt được thì không có lựa chọn nào khác; bước nhảy phải được thực hiện. Nếu có nhiều hơn một lần bắt, người chơi có thể tự do lựa chọn hướng đi của mình. <br /> <br />" +

				"6. Khi một quân cờ đến hàng xa nhất tính từ người chơi điều khiển quân cờ đó, quân cờ đó sẽ được trao vương miện và trở thành vua. <br /> <br />"
				+ "7. Các vị vua di chuyển như quân nhưng có thể di chuyển cả về phía trước và phía sau. <br /> <br />"
				+ "8. Vua có thể kết hợp các bước nhảy theo nhiều hướng, tiến và lùi trong cùng một lượt. Các quân đơn lẻ có thể chuyển hướng theo đường chéo trong một lượt bắt nhiều lần, nhưng phải luôn tiến về phía trước (về phía đối thủ).";

		JOptionPane.showMessageDialog(this, "<html><body><p style='width: 400px'>" + message + "</p></body></html>",
				"", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	private void onUndoClick() {
		game.undo();
		updateCheckerBoard();
		if (main.gui.Settings.hintMode) {
			onHintClick();
		}
	}
	
	private void onHintClick() {
		if (!game.isGameOver()) {

			AI ai = new AI(7, Player.HUMAN);
			helpMoves = null;

			hintMove = ai.move(this.game.getState(), Player.HUMAN, "");
			updateCheckerBoard();
		}
	}
	/***************************************************************/
	/*********************** ON CLICK METHODS **********************/
	
	public void onMouseRelease(int position, int dx, int dy) {

		MoveFeedback feedback = game.playerMove(position, dx, dy);
		
		if (feedback == MoveFeedback.SUCCESS) {
			updateCheckerBoard();

			aiMove();
		} else {
		
			updateCheckerBoard();
			System.out.println(feedback.toString());
		}
	}

	
	private void onHelpMovablesClick() {
		hintMove = null;

		helpMoves = game.getState().getSuccessors().stream().map(x -> x.getFromPos()).collect(Collectors.toList());
		updateCheckerBoard();
	}
		private void onHelpModeClick() {
		main.gui.Settings.helpMode = !main.gui.Settings.helpMode;
		System.out.println("Chế độ trợ giúp: " + main.gui.Settings.helpMode);
	}

	private void onHintModeClick() {
		main.gui.Settings.hintMode = !main.gui.Settings.hintMode;
		System.out.println("Chế độ gợi ý: " + main.gui.Settings.hintMode);
		onHintClick();
	}

	private void onPieceClick(ActionEvent actionEvent) {

		if (game.getTurn() == Player.HUMAN) {

			CheckerButton button = (CheckerButton) actionEvent.getSource();
			int pos = button.getPosition();

			if (button.getPiece().getPlayer() == Player.HUMAN) {

				possibleMoves = game.getValidMoves(pos);

				updateCheckerBoard();

				if (possibleMoves.size() == 0) {

					MoveFeedback feedback = game.moveFeedbackClick(pos);

					updateText(feedback.toString());

					if (feedback == MoveFeedback.FORCED_JUMP) {

						onHelpMovablesClick();
					}
				}

				else {
					updateText("");
				}
			}
		}
	}

	private void onGhostButtonClick(ActionEvent actionEvent) {
		if (!game.isGameOver() && game.getTurn() == Player.HUMAN) {
			hintMove = null;
			helpMoves = null;

			GhostButton button = (GhostButton) actionEvent.getSource();

			game.playerMove(button.getBoardstate());
			possibleMoves = new ArrayList<>();

			updateCheckerBoard();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

					aiMove();

					if (game.isGameOver()) {
						gameOver();
					}
				}
			});
		}
	}
	
	private void gameOver() {
		JOptionPane.showMessageDialog(this, game.getGameOverMessage(), "", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void aiMove() {

		long startTime = System.nanoTime();

		game.aiMove();
		
		long aiMoveDurationInMs = (System.nanoTime() - startTime) / 1000000;

		long delayInMs = Math.max(0, main.gui.Settings.AiMinPauseDurationInMs - aiMoveDurationInMs);

		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

		exec.schedule(new Runnable() {
			@Override

			public void run() {
				invokeAiUpdate();
			}
		}, delayInMs, TimeUnit.MILLISECONDS);
	}

	
	private void invokeAiUpdate() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				updateCheckerBoard();

				if (!game.isGameOver() && game.getTurn() == Player.AI) {
					aiMove();
				} else if (main.gui.Settings.hintMode) {

					onHintClick();
				}
			}
		});
	}

	

	

}
