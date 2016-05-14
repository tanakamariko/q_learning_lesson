package q_learning_lesson;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * @author Tanaka
 *
 */
public class Main {

  //マップの定義
  static int map[][] = { { -10, 0, 0, 0, 10 } };
  //TODO 壁はどうする？

  //マップのサイズ
  static final int MAP_ROW_SIZE = 1;
  static final int MAP_COLUMN_SIZE = 5;

  //スタートの座標
  static final int START_ROW_NUM = 0;
  static final int START_COLUMN_NUM = 2;

  //上，左，右，下 各行動の定数の割当
  static final int ACTION_MOVE_UP = 1;
  static final int ACTION_MOVE_LEFT = 2;
  static final int ACTION_MOVE_RIGHT = 4;
  static final int ACTION_MOVE_DOWN = 8;


  static final double LEARNING_RATE = 0.1;
  static final double DISCOUNT_FACTOR = 0.9;
  static final double EPSILON = 0.3;

  //Qテーブルの定義
  static QCell[][] qTable = new QCell[MAP_ROW_SIZE][MAP_COLUMN_SIZE];

  //現在位置
  static int rowNum = START_ROW_NUM;
  static int columnNum = START_COLUMN_NUM;

  //ランダム
  private static Random rnd = new Random();

  private static boolean goaledFlag = false;


  public static void main(String args[]){

    //qTableの初期化
    for (int i = 0; i < MAP_ROW_SIZE; i++) {
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        int map_state = map[i][j];
        qTable[i][j] = new QCell(map_state);
      }
    }
    printQTable();

    for (int i = 0; i < 600; i++) {
      if(goaledFlag){
        goaledFlag = false;
        rowNum = START_ROW_NUM;
        columnNum = START_COLUMN_NUM;
      }
      QCell newQCell = qTable[rowNum][columnNum];
      int canMoveBin = canMove(rowNum, columnNum);
      calcFlagedQValue(newQCell, canMoveBin);
      int selectedAction = selectGreedy(newQCell, canMoveBin);
      calcFlagedQValue(qTable[rowNum][columnNum], selectedAction);
      updateCurrentCoordinate(selectedAction);
      if(map[rowNum][columnNum] != 0){
        goaledFlag = true;
      }

      printQTable();
    }

  }


  //Q値を計算する
  static double calcQValue(double oldValue, QCell nextCell){
    return oldValue + LEARNING_RATE * (nextCell.getMapState() + DISCOUNT_FACTOR * nextCell.getMaxQValue() - oldValue);
  }

  static void calcFlagedQValue(QCell currentCell, int canMoveBin){
    String canMoveBinStr =String.format("%4s", Integer.toBinaryString(canMoveBin)).replace(' ', '0');
    if(canMoveBinStr.charAt(3) == '1'){
      currentCell.setUpQValue(calcQValue(qTable[rowNum][columnNum].getUpQValue(), qTable[rowNum + 1][columnNum]));
    }
    if(canMoveBinStr.charAt(2) == '1'){
      currentCell.setLeftQValue(calcQValue(qTable[rowNum][columnNum].getLeftQValue(), qTable[rowNum][columnNum - 1]));
    }
    if(canMoveBinStr.charAt(1) == '1'){
      currentCell.setRightQValue(calcQValue(qTable[rowNum][columnNum].getRightQValue(), qTable[rowNum][columnNum + 1]));
    }
    if(canMoveBinStr.charAt(0) == '1'){
      currentCell.setDownQValue(calcQValue(qTable[rowNum][columnNum].getDownQValue(), qTable[rowNum + 1][columnNum]));
    }
  }

  //グリーディ法．最大のQ値となる行動を選択(最大のQ値となる行動が複数の場合はその中からランダム)
  static int selectGreedy(QCell newQCell, int canMoveBin){
    int maxQValueAction = 0;
    double maxQValue = newQCell.getMaxQValue();
    if(maxQValue == newQCell.getUpQValue()){
      maxQValueAction += ACTION_MOVE_UP;
    }
    if(maxQValue == newQCell.getLeftQValue()){
      maxQValueAction += ACTION_MOVE_LEFT;
    }
    if(maxQValue == newQCell.getRightQValue()){
      maxQValueAction += ACTION_MOVE_RIGHT;
    }
    if(maxQValue == newQCell.getDownQValue()){
      maxQValueAction += ACTION_MOVE_DOWN;
    }
    int selectCandidate = canMoveBin & maxQValueAction; //selectCandidate == 0 になることは無いはず？
    int selected = 0;
    while(selected == 0){
      selected = selectCandidate & (int)Math.pow(2, rnd.nextInt(4));
    }
    return selected;
  }

  private static void updateCurrentCoordinate(int selectedAction) {
    String selectedActionStr =String.format("%4s", Integer.toBinaryString(selectedAction)).replace(' ', '0');
    if(selectedActionStr.charAt(3) == '1'){
      rowNum = rowNum + 1;
    }
    if(selectedActionStr.charAt(2) == '1'){
      columnNum = columnNum - 1;
    }
    if(selectedActionStr.charAt(1) == '1'){
      columnNum = columnNum + 1;
    }
    if(selectedActionStr.charAt(0) == '1'){
      rowNum = rowNum + 1;
    }
  }

  /*
   * 現在の座標から各方向に進めるか調べる関数
   * とりあえずマップサイズ外じゃないかだけ判定(壁は考慮なし)
   */
  static int canMove(int rowNum, int columnNum) {
    int canMoveBin = 0;

    int nextRowNum = rowNum - 1;
    int nextColumnNum = columnNum;
    if (0 <= nextRowNum && nextRowNum < MAP_ROW_SIZE && 0 <= nextColumnNum && nextColumnNum < MAP_COLUMN_SIZE) {
      canMoveBin += 1;
    }

    nextRowNum = rowNum;
    nextColumnNum = columnNum - 1;
    if (0 <= nextRowNum && nextRowNum < MAP_ROW_SIZE && 0 <= nextColumnNum && nextColumnNum < MAP_COLUMN_SIZE) {
      canMoveBin += 2;
    }

    nextRowNum = rowNum;
    nextColumnNum = columnNum + 1;
    if (0 <= nextRowNum && nextRowNum < MAP_ROW_SIZE && 0 <= nextColumnNum && nextColumnNum < MAP_COLUMN_SIZE) {
      canMoveBin += 4;
    }

    nextRowNum = rowNum + 1;
    nextColumnNum = columnNum;
    if (0 <= nextRowNum && nextRowNum < MAP_ROW_SIZE && 0 <= nextColumnNum && nextColumnNum < MAP_COLUMN_SIZE) {
      canMoveBin += 8;
    }

    return canMoveBin;
  }

  //qTableをいい感じにコンソールにプリントする関数
  static void printQTable(){
    DecimalFormat df = new DecimalFormat(" 0.00");
    for (int i = 0; i < MAP_ROW_SIZE; i++) {
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        System.out.print("-----------");
      }
      System.out.println();
      System.out.print(" | ");
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        System.out.print("↑"+ df.format(qTable[i][j].getUpQValue()));
        System.out.print(" | ");
      }
      System.out.println();
      System.out.print(" | ");
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        System.out.print("←"+ df.format(qTable[i][j].getLeftQValue()));
        System.out.print(" | ");
      }
      System.out.println();
      System.out.print(" | ");
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        System.out.print("→"+ df.format(qTable[i][j].getRightQValue()));
        System.out.print(" | ");
      }
      System.out.println();
      System.out.print(" | ");
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        System.out.print("↓"+ df.format(qTable[i][j].getDownQValue()));
        System.out.print(" | ");
      }
      System.out.println();
      for (int j = 0; j < MAP_COLUMN_SIZE; j++) {
        System.out.print("-----------");
      }
      System.out.println();
    }

  }
}