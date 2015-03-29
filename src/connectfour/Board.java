package connectfour;

import java.util.Scanner;
import java.net.*; // multiplayer
import java.io.*;

public class Board {
	private enum Player { ONE, TWO };
	
	Player player;
	Piece[][] pieces;
	int[] tops;
	
	Socket connectionSocket;
	BufferedReader inFromUser;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	
	public Board() {
		player = Player.ONE;
		pieces = new Piece[6][7];
		tops = new int[6];
		for(int i = 0; i < 6; i++) {
			tops[i] = 5;
		}
	}
	
	public void insertPiece(int col) throws ArrayIndexOutOfBoundsException {
		if(col < 0 || col > 6 || tops[col] < 0) {
			throw new ArrayIndexOutOfBoundsException();
		} else {
			pieces[tops[col]][col] = new Piece(playerColor());
			tops[col]--;
		}
	}
	
	public PieceType playerColor() { // determines piece color by player number
		if(player == Player.ONE) {
			return PieceType.RED;
		} else {
			return PieceType.YELLOW;
		}
	}
	
	public void nextPerson() {
		if(player == Player.ONE) {
			player = Player.TWO;
		} else {
			player = Player.ONE;
		}
	}
	
	public void takeTurn() {
		Scanner s = new Scanner(System.in);
		System.out.println("Player " +  player + " pick a column:");
		int choice = s.nextInt();
		try {
			insertPiece(choice);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("FAIL: you lost your turn");
		}
		printBoard();
	}
	
	public void start() {
		while(!checkWin()) {
			takeTurn();
			nextPerson();
		}
		nextPerson();
		System.out.println("GAME OVER: Player " + player + " won!");
	}
	
	public boolean checkWin() {
		return checkHoriz() || checkVert() || checkDiag();
	}
	
	private boolean checkHoriz() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 4; j++) {
				if(pieces[i][j] != null && pieces[i][j+1] != null 
						&& pieces[i][j+2] != null
						&& pieces[i][j+3] != null
						&& pieces[i][j].color == pieces[i][j+1].color
						&& pieces[i][j].color == pieces[i][j+2].color
						&& pieces[i][j].color == pieces[i][j+3].color)
					return true;
			}
		}
		return false;
	}
	
	private boolean checkVert() {
		for(int j = 0; j < 7; j++) {
			for(int i = 0; i < 3; i++) {
				if(pieces[i][j] != null && pieces[i+1][j] != null 
						&& pieces[i+2][j] != null
						&& pieces[i+3][j] != null
						&& pieces[i][j].color == pieces[i+1][j].color
						&& pieces[i][j].color == pieces[i+2][j].color
						&& pieces[i][j].color == pieces[i+3][j].color)
					return true;
			}
		}
		return false;
	}
	
	private boolean checkDiag() { // super janky because i got lazy
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 7; j++) {
				try { // '\'
					if(pieces[i][j].color == pieces[i+1][j+1].color
							&& pieces[i][j].color == pieces[i+2][j+2].color
							&& pieces[i][j].color == pieces[i+3][j+3].color)
						return true;
				} catch(Exception e) {}
				try { // '/'
					if(pieces[i][j].color == pieces[i+1][j-1].color
							&& pieces[i][j].color == pieces[i+2][j-2].color
							&& pieces[i][j].color == pieces[i+3][j-3].color)
						return true;
				} catch(Exception e) {}
			}
		}
		return false;
	}
	
	public void printBoard() {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 7; j++) {
				if(pieces[i][j] == null) {
					System.out.print("* ");
				} else if(pieces[i][j].color == PieceType.RED) {
					System.out.print("R ");
				} else {
					System.out.print("Y ");
				}
			}
			System.out.println();
		}
	}
	
	// multiplayer functionality
	
	public void establishConnection() throws IOException {
		ServerSocket welcomeSocket = new ServerSocket(6789);
		System.out.println("Waiting to connect with client...");
		connectionSocket = welcomeSocket.accept();
		
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		outToClient = new DataOutputStream(connectionSocket.getOutputStream());
	}
	
	public void startMulti() throws IOException {
		establishConnection();
		while(!checkWin()) {
			takeMultiTurn();
			nextPerson();
		}
		nextPerson();
		System.out.println("GAME OVER: Player " + player + " won!");
		outToClient.writeBytes("GAME OVER: Player " + player + " won!");
		connectionSocket.close();
	}
	
	public void takeMultiTurn() throws IOException {
		if(player == Player.ONE) {
			outToClient.writeBytes("Player one's turn\n");
			takeTurn();
			printBoardMulti(outToClient);
		} else {
			System.out.println("Waiting on player 2...");
			outToClient.writeBytes("Your turn! Choose a column: \n");
			outToClient.writeBytes("GO\n");
			try {
				int choice = Integer.parseInt(inFromClient.readLine());
				insertPiece(choice);
			} catch(Exception e) {
				outToClient.writeBytes("FAIL: you lost your turn");
			}
			printBoard();
			printBoardMulti(outToClient);
		}
	}
	
	public void printBoardMulti(DataOutputStream out) throws IOException {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 7; j++) {
				if(pieces[i][j] == null) {
					out.writeBytes("* ");
				} else if(pieces[i][j].color == PieceType.RED) {
					out.writeBytes("R ");
				} else {
					out.writeBytes("Y ");
				}
			}
			out.writeBytes("\n");
		}
	}
		
}
