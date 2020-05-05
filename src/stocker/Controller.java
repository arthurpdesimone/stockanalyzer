package stocker;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import stocker.stock.Benchmark;
import stocker.stock.Stock;
import stocker.stock.StockManager;
import stocker.stock.StockOperation;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static stocker.Utils.download;
import static stocker.Utils.reOrderListByDate;
import static stocker.stock.StockOperation.BUY;
import static stocker.stock.StockOperation.SELL;

public class Controller {
    /** API KEY*/
    String API_KEY = "D40ZO7YMXDL464DI";
    /** API URL */
    String URL = "https://www.alphavantage.co/query?function=";
    @FXML
    Button downloadStocks;
    @FXML
    LineChart<String,Number> chart;
    @FXML
    LineChart<String,Number> RSIChart;
    @FXML
    LineChart<String,Number> MACDChart;
    @FXML
    TextArea console;
    @FXML
    TextField SMA1;
    @FXML
    TextField SMA2;
    @FXML
    TextField MACDSlow;
    @FXML
    TextField MACDFast;
    @FXML
    TextField MACDSignal;
    @FXML
    TextField RSI;
    @FXML
    ChoiceBox<String> tickerChoice;
    /** Time Frame Buttons */
    @FXML
    Button FiveDays;
    @FXML
    Button OneMonth;
    @FXML
    Button ThreeMonths;
    @FXML
    Button SixMonths;
    @FXML
    Button OneYear;
    @FXML
    Button FiveYears;
    @FXML
    TableView<Benchmark> benchmarks;
    @FXML
    TableColumn<Benchmark,String> benchmarksStock;
    @FXML
    TableColumn<Benchmark,String> benchmarksOperations;
    @FXML
    TableColumn<Benchmark,String> benchmarksProfit;
    @FXML
    TableColumn<Benchmark,String> benchmarksBuyAndHold;
    @FXML
    TableView<StockOperation> operations;
    @FXML
    TableColumn<StockOperation,String> operationsDate;
    @FXML
    TableColumn<StockOperation,String> operationsStock;
    @FXML
    TableColumn<StockOperation,String> operationsType;
    @FXML
    TableColumn<StockOperation,String> operationsValue;
    @FXML
    ListView<String> tickerList;
    @FXML
    ListView<String> stocksList;
    @FXML
    TextField addTickerTextField;
    @FXML
    Button addTickerButton;
    @FXML
    ProgressBar downloadProgressBar;
    @FXML
    CheckBox SMA1Enabled;
    @FXML
    CheckBox SMA2Enabled;
    @FXML
    CheckBox MACDEnabled;
    @FXML
    CheckBox RSIEnabled;

    /** Log variable */
    StringBuilder logger = new StringBuilder();
    /** Log to keep track of time events*/
    long currentTime = 0;
    /** Pause time*/
    static int PAUSE = 12000;
    /** Map containing tickers and stocks*/
    HashMap<String,ArrayList<Stock>> stocks = StockManager.getInstance().getStocks();
    /** Time frame */
    int timeFrame = StockManager.getInstance().getTimeFrame();
    /** Stock timeline */
    ArrayList<StockOperation> cashFlow = StockManager.getInstance().getCashFlow();
    /** Benchmarks */
    ArrayList<Benchmark> benchmarkList = StockManager.getInstance().getBenchmarks();
    /** Download steps*/
    double downloadSteps = 0;
    /** Observable cashflow */
    ObservableList<StockOperation> observableCashFlow;
    /** Observable benchmark */
    ObservableList<Benchmark> observableBenchmark;
    @FXML
    public void initialize(){
        currentTime = System.currentTimeMillis();
        /** Progress bar configuration*/
        downloadProgressBar.setMaxWidth(Double.MAX_VALUE);
        downloadProgressBar.getStylesheets().add(getClass().getResource("striped-progress.css").toExternalForm());

        /** Ticker list initialization */
        try {
            FileInputStream fis = new FileInputStream("stocks.txt");
            Scanner sc = new Scanner(fis);
            while(sc.hasNextLine()) {
                tickerList.getItems().add(sc.nextLine());
            }

            /** Context menu creation */
            tickerList.setCellFactory(param -> {
                ListCell<String> cell = new ListCell<>();
                ContextMenu contextMenu = new ContextMenu();

                MenuItem deleteItem = new MenuItem();
                deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
                deleteItem.setOnAction(event -> tickerList.getItems().remove(cell.getItem()));
                contextMenu.getItems().addAll(deleteItem);

                cell.textProperty().bind(cell.itemProperty());

                cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                    if (isNowEmpty) {
                        cell.setContextMenu(null);
                    } else {
                        cell.setContextMenu(contextMenu);
                    }
                });

                return cell;
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log(e.getMessage());
        }

        /** Add ticker button configuration */
        addTickerButton.setOnAction(event -> {
            tickerList.getItems().add(addTickerTextField.getText());
            addTickerTextField.clear();
        });

        /** Main sequence download */
        downloadStocks.setOnAction(event -> {
            /** Steps count */
            if(SMA1Enabled.isSelected()) downloadSteps++;
            if(SMA2Enabled.isSelected()) downloadSteps++;
            if(MACDEnabled.isSelected()) downloadSteps++;
            if(RSIEnabled.isSelected()) downloadSteps++;
            downloadSteps = downloadSteps*tickerList.getItems().size();
            downloadSteps = downloadSteps + tickerList.getItems().size();

            log("Steps detected : "+downloadSteps);

            /** Steps */
            final double[] stepsConsumed = {0.0};

            Thread thread = new Thread(() -> tickerList.getItems().forEach(ticker -> {
                try{
                    Thread.sleep(PAUSE);
                    tickerChoice.getItems().add(ticker);
                    stocks.put(ticker,new ArrayList<Stock>());
                    String fileName = ticker.replace(".SA","")+".csv";
                    /**----------------------------- STOCK ----------------------------- */
                    log("Downloading "+ticker);
                    /** Download stocks prices */
                    download(URL+"TIME_SERIES_DAILY&symbol="+ticker+"&outputsize=full&datatype=csv&apikey="+API_KEY,ticker.replace(".SA","")+".csv");

                    log("Downloaded "+ticker);
                    /** Progress update */
                    stepsConsumed[0]++;
                    downloadProgressBar.setProgress(stepsConsumed[0]/downloadSteps);

                    /** Stock download*/
                    BufferedReader timeSeries = new BufferedReader(new FileReader(fileName));
                    String row;
                    while ((row = timeSeries.readLine()) != null) {
                        String[] data = row.split(",");
                        String date = data[0];
                        String close = data[4];
                        String pattern = "yyyy-MM-dd";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        try {
                            Date d = simpleDateFormat.parse(date);
                            Double closeValue = Double.parseDouble(close);
                            Stock stock = new Stock(ticker,closeValue,d);
                            stocks.get(ticker).add(stock);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                    timeSeries.close();

                    /** Stocks list fulfillment*/
                    Stock lastStock = stocks.get(ticker).get(0);
                    Stock firstStock = stocks.get(ticker).get(stocks.get(ticker).size()-1);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String firstDate = formatter.format(firstStock.getDate());
                    String lastDate = formatter.format(lastStock.getDate());
                    Platform.runLater(() -> {
                        stocksList.getItems().add(ticker+" First stock: "+firstDate+"\t Last Stock: "+lastDate+"\t Entries: "+stocks.get(ticker).size());
                    });


                    if(SMA1Enabled.isSelected()){
                        Thread.sleep(PAUSE);
                        /**----------------------------- SMA 1 ----------------------------- */
                        log("Downloading SMA1 for "+ticker);
                        download(URL+"SMA&symbol="+ticker+"&interval=daily&time_period="+SMA1.getText()+"&series_type=close&datatype=csv&apikey="+API_KEY,ticker.replace(".SA","")+"-SMA1.csv");
                        BufferedReader SMA1Series = new BufferedReader(new FileReader(ticker.replace(".SA","")+"-SMA1.csv"));
                        String rowSMA1;
                        while ((rowSMA1 = SMA1Series.readLine()) != null) {
                            String[] data = rowSMA1.split(",");
                            String date = data[0];
                            String sma1 = data[1];
                            String pattern = "yyyy-MM-dd";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            try {
                                Date d = simpleDateFormat.parse(date);
                                Double SMA1Value = Double.parseDouble(sma1);
                                /** Loop through all the stocks looking for a matching date*/
                                stocks.get(ticker).forEach(stock -> {
                                    if(d.equals(stock.getDate())){
                                        stock.setSMA1(SMA1Value);
                                    }
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        SMA1Series.close();
                        log("Downloaded SMA1 for "+ticker);
                        stepsConsumed[0]++;
                        downloadProgressBar.setProgress(stepsConsumed[0]/downloadSteps);
                    }

                    if(SMA2Enabled.isSelected()){
                        /**----------------------------- SMA 2 ----------------------------- */
                        Thread.sleep(PAUSE);
                        log("Downloading SMA2 for "+ticker);
                        download(URL+"SMA&symbol="+ticker+"&interval=daily&time_period="+SMA2.getText()+"&series_type=close&datatype=csv&apikey="+API_KEY,ticker.replace(".SA","")+"-SMA2.csv");
                        BufferedReader SMA2Series = new BufferedReader(new FileReader(ticker.replace(".SA","")+"-SMA2.csv"));
                        String rowSMA2;
                        while ((rowSMA2 = SMA2Series.readLine()) != null) {
                            String[] data = rowSMA2.split(",");
                            String date = data[0];
                            String sma2 = data[1];
                            String pattern = "yyyy-MM-dd";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            try {
                                Date d = simpleDateFormat.parse(date);
                                Double SMA2Value = Double.parseDouble(sma2);
                                /** Loop through all the stocks looking for a matching date*/
                                stocks.get(ticker).forEach(stock -> {
                                    if(d.equals(stock.getDate())){
                                        stock.setSMA2(SMA2Value);
                                    }
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        SMA2Series.close();
                        log("Downloaded SMA2 for "+ticker);
                        stepsConsumed[0]++;
                        downloadProgressBar.setProgress(stepsConsumed[0]/downloadSteps);
                    }
                    if(MACDEnabled.isSelected()){
                        /**----------------------------- MACD ----------------------------- */
                        Thread.sleep(PAUSE);
                        log("Downloading MACD for "+ticker);
                        download(URL+"MACD&symbol="+ticker+"&fastperiod="+MACDFast.getText()+"&slowperiod="+MACDSlow.getText()+"&signalperiod="+MACDSignal.getText()+"&interval=daily&series_type=close&datatype=csv&apikey="+API_KEY,ticker.replace(".SA","")+"-MACD.csv");
                        BufferedReader MACDSeries = new BufferedReader(new FileReader(ticker.replace(".SA","")+"-MACD.csv"));
                        String MACDRow;
                        while ((MACDRow = MACDSeries.readLine()) != null) {
                            String[] data = MACDRow.split(",");
                            String date = data[0];
                            String macd = data[1];
                            String macdHist = data[2];
                            String macdSignal = data[3];

                            String pattern = "yyyy-MM-dd";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            try {
                                Date d = simpleDateFormat.parse(date);
                                Double macdValue = Double.parseDouble(macd);
                                Double macdHistValue = Double.parseDouble(macdHist);
                                Double macdSignalValue = Double.parseDouble(macdSignal);
                                /** Loop through all the stocks looking for a matching date*/
                                stocks.get(ticker).forEach(stock -> {
                                    if(d.equals(stock.getDate())){
                                        stock.setMACD(macdValue);
                                        stock.setMACDHist(macdHistValue);
                                        stock.setMACDSignal(macdSignalValue);
                                    }
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        MACDSeries.close();
                        log("Downloaded MACD for "+ticker);
                        stepsConsumed[0]++;
                        downloadProgressBar.setProgress(stepsConsumed[0]/downloadSteps);
                    }
                    if(RSIEnabled.isSelected()){
                        /**----------------------------- RSI ----------------------------- */
                        Thread.sleep(PAUSE);
                        log("Downloading RSI for "+ticker);
                        download(URL+"RSI&symbol="+ticker+"&time_period="+RSI.getText()+"&interval=daily&series_type=close&datatype=csv&apikey="+API_KEY,ticker.replace(".SA","")+"-RSI.csv");
                        BufferedReader RSISeries = new BufferedReader(new FileReader(ticker.replace(".SA","")+"-RSI.csv"));
                        String RSIRow;
                        while ((RSIRow = RSISeries.readLine()) != null) {
                            String[] data = RSIRow.split(",");
                            String date = data[0];
                            String rsi = data[1];

                            String pattern = "yyyy-MM-dd";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            try {
                                Date d = simpleDateFormat.parse(date);
                                Double rsiValue = Double.parseDouble(rsi);
                                /** Loop through all the stocks looking for a matching date*/
                                stocks.get(ticker).forEach(stock -> {
                                    if(d.equals(stock.getDate())){
                                        stock.setRSI(rsiValue);
                                    }
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        RSISeries.close();
                        log("Downloaded RSI for "+ticker);
                        stepsConsumed[0]++;
                        downloadProgressBar.setProgress(stepsConsumed[0]/downloadSteps);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    log(e.getMessage());
                }

            }));
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        });

        /** Time frame actions */
        FiveDays.setOnAction(event -> {
            timeFrame = 5;
            calculate();
            fillChart();
        });
        OneMonth.setOnAction(event -> {
            timeFrame = 30;
            calculate();
            fillChart();
        });
        ThreeMonths.setOnAction(event -> {
            timeFrame = 90;
            calculate();
            fillChart();
        });
        SixMonths.setOnAction(event -> {
            timeFrame = 180;
            calculate();
            fillChart();
        });
        OneYear.setOnAction(event -> {
            timeFrame = 365;
            calculate();
            fillChart();
        });
        FiveYears.setOnAction(event -> {
            timeFrame = 1825;
            calculate();
            fillChart();
        });
    }

    /** Log routine
     * @param log The string to log*/
    public void log(String log){
        logger.append("["+(System.currentTimeMillis()-currentTime)+"]:"+log+"\n");
        console.clear();
        console.setText(logger.toString());
        console.setScrollTop(Double.MAX_VALUE);
        System.out.println(log);
    }

    /** Method to fill chart*/
    private void fillChart(){
        /** Closes stock configuration */
        XYChart.Series closes = new XYChart.Series();
        XYChart.Series SMA1Series = new XYChart.Series();
        XYChart.Series SMA2Series = new XYChart.Series();
        XYChart.Series MACDSeries = new XYChart.Series();
        XYChart.Series MACDHistogramSeries = new XYChart.Series();
        XYChart.Series MACDSignalSeries = new XYChart.Series();
        XYChart.Series RSISeries = new XYChart.Series();

        chart.getXAxis().setAutoRanging(true);
        chart.getYAxis().setAutoRanging(true);
        chart.setCreateSymbols(false);
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setForceZeroInRange(false);
        String ticker = tickerChoice.getSelectionModel().getSelectedItem();
        closes.setName(ticker);
        ArrayList<Stock> stocksList = stocks.get(ticker);

        /** SMAs creation*/
        if(SMA1Enabled.isSelected()){
            SMA1Series.setName("SMA1");
        }
        if(SMA2Enabled.isSelected()) {
            SMA2Series.setName("SMA2");
        }
        /** MACD configuration */
        if(MACDEnabled.isSelected()){
            MACDChart.getXAxis().setAutoRanging(true);
            MACDChart.getYAxis().setAutoRanging(true);
            MACDChart.setCreateSymbols(false);
            MACDSeries.setName("MACD");
            MACDHistogramSeries.setName("MACD Histogram");
            MACDSignalSeries.setName("MACD Signal");
        }

        /** RSI configuration */
        if(RSIEnabled.isSelected()){
            RSIChart.getXAxis().setAutoRanging(true);
            RSIChart.getYAxis().setAutoRanging(true);
            RSISeries.setName(ticker);
        }
        /** Clear all previous data */
        closes.getData().removeAll(Collections.singleton(chart.getData().setAll()));
        MACDChart.getData().removeAll(Collections.singleton(MACDChart.getData().setAll()));
        RSIChart.getData().removeAll(Collections.singleton(RSIChart.getData().setAll()));

        /** Loop through all the stocks at a given timeframe*/
        for (int i = timeFrame;i >= 0; i--) {
            Stock stock = stocksList.get(i);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale. getDefault()). format(stock.getDate());

            /** Closes and SMA chart */
            XYChart.Data<String,Number> close = new XYChart.Data(date, stock.getClose());
            close.setNode(new HoveredThresholdNodeLabel(stock));
            closes.getData().add(close);

            if(SMA1Enabled.isSelected()){
                XYChart.Data<String,Number> SMA1Value = new XYChart.Data(date, stock.getSMA1());
                SMA1Series.getData().add(SMA1Value);
            }
            if(SMA2Enabled.isSelected()) {
                XYChart.Data<String, Number> SMA2Value = new XYChart.Data(date, stock.getSMA2());
                SMA2Series.getData().add(SMA2Value);
            }
            if(MACDEnabled.isSelected()){
                /** MACD Histogram */
                XYChart.Data<String,Number> MACDValue = new XYChart.Data(date, stock.getMACD());
                XYChart.Data<String,Number> MACDHistValue = new XYChart.Data(date, stock.getMACDHist());
                XYChart.Data<String,Number> MACDSignalValue = new XYChart.Data(date, stock.getMACDSignal());
                MACDHistogramSeries.getData().add(MACDHistValue);
                MACDSeries.getData().add(MACDValue);
                MACDSignalValue.setNode(new NodeBuySell(stock));
                MACDSignalSeries.getData().add(MACDSignalValue);
            }
            if(RSIEnabled.isSelected()){
                /** RSI Value */
                XYChart.Data<String,Number> RSIValue = new XYChart.Data(date, stock.getRSI());
                RSISeries.getData().add(RSIValue);
            }
        }

        chart.getData().addAll(closes);
        if(SMA1Enabled.isSelected()) chart.getData().addAll(SMA1Series);
        if(SMA2Enabled.isSelected()) chart.getData().addAll(SMA2Series);
        if(MACDEnabled.isSelected()) MACDChart.getData().addAll(MACDSeries,MACDHistogramSeries,MACDSignalSeries);
        if(RSIEnabled.isSelected()) RSIChart.getData().addAll(RSISeries);
    }

    private void fillOperationsTable(){
       reOrderListByDate(cashFlow);
       observableCashFlow = FXCollections.observableArrayList(cashFlow);
       /** Operations table*/
       operations.setItems(observableCashFlow);
       operationsDate.setSortable(false);
       operationsStock.setSortable(false);
       operationsType.setSortable(false);
       operationsValue.setSortable(false);
       operationsDate.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getDate().toString()));
       operationsStock.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getTicker()));
       operationsType.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getOperationType()));
       operationsValue.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock().getClose().toString()));
       customiseFactoryOperations(operationsType);
    }

    private void fillBenchmarkTable(){
        observableBenchmark = FXCollections.observableArrayList(benchmarkList);
        /** Operations table*/
        benchmarks.setItems(observableBenchmark);
        benchmarksStock.setSortable(false);
        benchmarksOperations.setSortable(false);
        benchmarksProfit.setSortable(false);
        benchmarksBuyAndHold.setSortable(false);
        benchmarksStock.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStock()));
        benchmarksOperations.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getOperationsBuyAndSell()+""));
        benchmarksProfit.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProfit()+"%"));
        benchmarksBuyAndHold.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBuyAndHold()+"%"));
        customiseFactoryBenchmark(benchmarksProfit);
    }

    private void customiseFactoryBenchmark(TableColumn<Benchmark, String> benchmarkColumn) {
        benchmarkColumn.setCellFactory(column -> {
            return new TableCell<Benchmark, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);

                    TableRow<Benchmark> currentRow = getTableRow();

                    if (!isEmpty()) {
                        item = item.replace("%","");
                        item = item.replace(",",".");
                        double value = Double.valueOf(item);
                        if(value < 0)
                            currentRow.setStyle("-fx-background-color:lightcoral");
                        else
                            currentRow.setStyle("-fx-background-color:lightgreen");
                    }
                }
            };
        });
    }



    private void customiseFactoryOperations(TableColumn<StockOperation, String> operationsStockColumn) {
        operationsStockColumn.setCellFactory(column -> {
            return new TableCell<StockOperation, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);

                    TableRow<StockOperation> currentRow = getTableRow();

                    if (!isEmpty()) {
                        if(item.equals("BUY"))
                            currentRow.setStyle("-fx-background-color:lightcoral");
                        else
                            currentRow.setStyle("-fx-background-color:lightgreen");
                    }
                }
            };
        });
    }


    /** a node which displays a value on hover, but is otherwise empty */
    class HoveredThresholdNodeLabel extends StackPane {
        HoveredThresholdNodeLabel(Stock stock) {
            setPrefSize(5, 5);
            createBuyAndSellSignal(this,stock);
            final Label label = createDataThresholdLabel(stock.getClose());

            setOnMouseEntered(mouseEvent -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                createBuyAndSellSignal(this,stock);
                setCursor(Cursor.CROSSHAIR);
            });
        }
    }
    /** a node which displays a value on hover, but is otherwise empty */
    class NodeBuySell extends StackPane {
        NodeBuySell(Stock stock) {
            setPrefSize(0, 0);
            createBuyAndSellSignal(this,stock);
        }
    }

    private void createBuyAndSellSignal(Pane pane, Stock stock){
        cashFlow.forEach(stockOperation -> {
            if(stockOperation.getStock().equals(stock)){
                Polygon polygon = new Polygon();

                //Adding coordinates to the polygon
                polygon.getPoints().addAll(new Double[]{
                        -5.0, -5.0,
                        5.0, -5.0,
                        0.0, 5.0
                });

                if(stockOperation.getOperationType().equals(BUY)){
                    polygon.setFill(Color.RED);
                }else if(stockOperation.getOperationType().equals(SELL)){
                    Rotate rotate = new Rotate();
                    rotate.setAngle(180);
                    rotate.setPivotX(0);
                    rotate.setPivotY(0);
                    polygon.getTransforms().add(rotate);
                    polygon.setFill(Color.GREEN);
                }
                pane.getChildren().add(polygon);
            }
        });
    }

    private Label createDataThresholdLabel(double value) {
        final Label label = new Label(value + "");
        label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
        label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        label.setTextFill(javafx.scene.paint.Color.DARKGRAY);
        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }

    /** Main method to calculate buys and sells*/
    public void calculate(){
        cashFlow.clear();
        for(String ticker : stocks.keySet()){
            try{
                log("Calculating buy and sell opportunities for : " + ticker);
                /** Initializing variables */
                LinkedList<StockOperation> operations = new LinkedList<>();
                ArrayList<Stock> stocksList = stocks.get(ticker);
                ArrayList<Stock> stocksTimeFrame = new ArrayList<>();

                /** Looping thourgh the considered timeframe */
                for (int i = timeFrame;i >= 1; i--) {
                    Stock stock = stocksList.get(i);
                    Stock nextStock = stocksList.get(i-1);
                    /** Add the current stock to a time frame array*/
                    stocksTimeFrame.add(stock);
                    //log("Comparing "+stock.getDate().toString()+ " to " + nextStock.getDate().toString());
                    /** Detects an up crossing */
                    if(nextStock.getMACD()>nextStock.getMACDSignal() && stock.getMACD()<stock.getMACDSignal()){
                        if(operations.size() == 0 || operations.getLast().getOperationType().equals(SELL)){
                            StockOperation buy = new StockOperation(nextStock);
                            buy.setOperationType(BUY);
                            operations.add(buy);
                            log(nextStock.getTicker()+" Buy opportunity detected on "+nextStock.getDate().toString()+ " value : "+nextStock.getClose());
                        }
                    }/** Detects a down crossing*/
                    else if (nextStock.getMACD()<nextStock.getMACDSignal() && stock.getMACD()>stock.getMACDSignal()){
                        if(operations.size() != 0 && operations.getLast().getOperationType().equals(BUY)){
                            StockOperation sell = new StockOperation(nextStock);
                            sell.setOperationType(SELL);
                            operations.add(sell);
                            log(nextStock.getTicker()+" Sell opportunity detected on "+nextStock.getDate().toString()+ " value : "+nextStock.getClose());
                        }
                    }
                    if(i==1){
                        log("Last stock is "+nextStock.getDate().toString());
                        stocksTimeFrame.add(nextStock);
                        /** Sells last stock */
                        if(operations.getLast().getOperationType().equals(BUY)){
                            StockOperation sell = new StockOperation(nextStock);
                            sell.setOperationType(SELL);
                            operations.add(sell);
                            log(nextStock.getTicker()+" Sell opportunity detected on "+nextStock.getDate().toString()+ " value : "+nextStock.getClose());
                        }
                    }
                }
                /** Create a benchmark for this operation */
                Benchmark b = new Benchmark();
                b.setOperations(operations);
                b.setStocks(stocksTimeFrame);
                benchmarkList.add(b);

                cashFlow.addAll(operations);
            }catch (Exception e){
                log(e.getMessage());
            }
        }
        fillOperationsTable();
        fillBenchmarkTable();
    }
}
