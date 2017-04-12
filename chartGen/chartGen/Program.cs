using OfficeOpenXml;
using OfficeOpenXml.Drawing.Chart;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;

namespace chartGen
{
    internal class TickData
    {
        public enum DataType { QValue, Trial };

        public double qValue;
        public int trialNumber;

        public TickData(double qValue, int trialNumber)
        {
            this.qValue = qValue;
            this.trialNumber = trialNumber;
        }
    }

    internal class Program
    {
        private static ExcelPackage package;

        private static void Main(string[] args)
        {
            foreach (String s in args)
            {
                if (s.EndsWith(".csv"))
                {
                    try
                    {
                        string csv = "results/csv/" + s;
                        String xlsName = "results/xls/" + s.Substring(0, s.Length - 3) + "xlsx";
                        save(csv, xlsName);
                    }
                    catch(IOException)
                    {
                        Console.WriteLine("File " + s + "Is already opened somewhere");
                    }
                }
            }
        }

        private static void save(String csvName, String xlsName)
        {

            using (package = new ExcelPackage(new FileStream(xlsName, FileMode.Create)))
            {
                var data = readValues(csvName);

                var positions = getPositionList(data);
                foreach (Position p in positions)
                {
                    String start = p.row + " " + p.col + " ";
                    package.Workbook.Worksheets.Add("s(" + p.row + ", " + p.col + ")");
                    ExcelWorksheet sheet = package.Workbook.Worksheets[package.Workbook.Worksheets.Count];
                    
                    var actionsData =
                         from k in data
                         where k.Key.StartsWith(start)
                         select new KeyValuePair<String, List<TickData>>(k.Key.Substring(start.Length).ToLower(), k.Value);

                 int width, height, stepRow, stepCol;
                    width = 300; height = 200;
                    stepRow = 15;
                    stepCol = 30;
                    int actionNumber = 0;
                    var totalNumberOfTrials = actionsData.Select(a => a.Value.Count).Max();
                    for (int i = 1; i <= totalNumberOfTrials + 1; i++)
                    {
                        sheet.Cells["ZZ" + i].Value = i * 1000;
                    }
                    var rangeXAxis = sheet.Cells["ZZ1:ZZ" + totalNumberOfTrials + 1];
                    foreach (KeyValuePair<String, List<TickData>> pair in actionsData)
                    {
                        
                        var chartQValue = sheet.Drawings.AddChart(pair.Key.ToLower() + "Q-Value", OfficeOpenXml.Drawing.Chart.eChartType.Line);
                        var chartTrials = sheet.Drawings.AddChart(pair.Key.ToLower() + "Trials", OfficeOpenXml.Drawing.Chart.eChartType.Line);

                        chartQValue.Title.Text = "Q-values for " + sheet.Name.Substring(0, sheet.Name.Length-1) +"," +  pair.Key +")";
                        chartTrials.Title.Text = "Trials for " + sheet.Name.Substring(0, sheet.Name.Length - 1) + "," + pair.Key + ")";

                        writeValues(sheet, pair.Key, pair.Value);

                        String columnNameQValue = getColumnForAction(pair.Key, TickData.DataType.QValue);
                        String columnNameTrial = getColumnForAction(pair.Key, TickData.DataType.Trial);
                        var rangeQValues = sheet.Cells[columnNameQValue + "2:" + columnNameQValue + (pair.Value.Count + 1)];
                        var rangeTrials = sheet.Cells[columnNameTrial + "2:" + columnNameTrial + (pair.Value.Count + 1)];

                        var serQ = chartQValue.Series.Add(rangeQValues, rangeXAxis) as ExcelChartSerie;
                        serQ.Header = "Q-Values";

                        var serT = chartTrials.Series.Add(rangeTrials, rangeXAxis) as ExcelChartSerie;
                        serT.Header = "Trials";

                        PlaceAndResizeChart(chartQValue, pair.Key, TickData.DataType.QValue);
                        PlaceAndResizeChart(chartTrials, pair.Key, TickData.DataType.Trial);


                    }
                    var pieChart = sheet.Drawings.AddChart("Trial comparisons", eChartType.Pie) as ExcelPieChart;
                    
                    pieChart.Title.Text = "Trial comparisons";
                    pieChart.Series.Add(sheet.Cells["BA" + (totalNumberOfTrials + 1) + ":BD" + (totalNumberOfTrials + 1)], sheet.Cells["BA1:BD1"]);
                    pieChart.DataLabel.ShowCategory = true;
                    pieChart.DataLabel.ShowPercent = true;
                    pieChart.DataLabel.ShowLeaderLines = true;

                    var chartComparisonQValues = sheet.Drawings.AddChart(p.row +","+p.col + "Q-Value Comp", OfficeOpenXml.Drawing.Chart.eChartType.Line);
                    var chartComparisonTrials = sheet.Drawings.AddChart(p.row + "," + p.col + "Trials Comp", OfficeOpenXml.Drawing.Chart.eChartType.Line);
                    chartComparisonQValues.Title.Text = "Q-Values for " + sheet.Name;
                    chartComparisonTrials.Title.Text = "Trials for " + sheet.Name;
                    foreach (var pair in actionsData)
                    {
                        String columnNameQValue = getColumnForAction(pair.Key, TickData.DataType.QValue);
                        String columnNameTrial = getColumnForAction(pair.Key, TickData.DataType.Trial);
                        var rangeQValues = sheet.Cells[columnNameQValue + "2:" + columnNameQValue + (pair.Value.Count + 1)];
                        var rangeTrials = sheet.Cells[columnNameTrial + "2:" + columnNameTrial + (pair.Value.Count + 1)];
                        chartComparisonQValues.Series.Add(rangeQValues, rangeXAxis);
                        chartComparisonTrials.Series.Add(rangeTrials, rangeXAxis);
                        chartComparisonQValues.Series[chartComparisonQValues.Series.Count - 1].Header = pair.Key;
                        chartComparisonTrials.Series[chartComparisonTrials.Series.Count - 1].Header = pair.Key;

                    }
                    PlaceAndResizeChart(chartComparisonQValues, "comparison", TickData.DataType.QValue);
                    PlaceAndResizeChart(chartComparisonTrials, "comparison", TickData.DataType.Trial);



                }
                package.Save();
                package.Dispose();
            }
        }
        
        private static void writeValues(ExcelWorksheet sheet, String action, List<TickData> data)
        {
            String qValueColumn = getColumnForAction(action, TickData.DataType.QValue);
            String trialColumn = getColumnForAction(action, TickData.DataType.Trial);
            sheet.Cells[qValueColumn + 1].Value = action;
            sheet.Cells[trialColumn + 1].Value = action;
            for (int i = 2; i < data.Count + 2; i++)
            {
                sheet.Cells[qValueColumn + i].Value = data[i - 2].qValue;
                sheet.Cells[trialColumn + i].Value = data[i - 2].trialNumber;
            }
        }

        private static String getColumnForAction(String action, TickData.DataType type)
        {
            String s = "";
            if (type == TickData.DataType.QValue)
            {
                s += "A";
            }
            else
            {
                s += "B";
            }
            switch (action)
            {
                case "down":
                    s += "A";
                    break;

                case "up":
                    s += "B";
                    break;

                case "right":
                    s += "C";
                    break;

                case "left":
                    s += "D";
                    break;

                case "null":
                    s += "E";
                    break;
            }

            return s;
        }

        private static void createChart()
        {
        }

        private static Dictionary<String, List<TickData>> readValues(String csvFileName)
        {
            var fmt = new NumberFormatInfo();
            fmt.NegativeSign = "-";
            Dictionary<String, List<TickData>> data = new Dictionary<String, List<TickData>>();
            String line;
            System.IO.StreamReader file = new System.IO.StreamReader(csvFileName);
            while ((line = file.ReadLine()) != null)
            {
                //Console.WriteLine(line);
                String[] cellValues = line.Split(new char[] { ';' });
                List<TickData> ticks = new List<TickData>();
                for (int i = 1; i < cellValues.Length; ++i)
                {
                    String[] fields = cellValues[i].Split(new char[] { ' ' });
                    ticks.Add(new TickData(double.Parse(fields[0], fmt), int.Parse(fields[1])));
                }
                Console.WriteLine(cellValues[0]);
                data.Add(cellValues[0], ticks);
            }

            file.Close();
            return data;
        }

        private static List<Position> getPositionList(Dictionary<String, List<TickData>> data)
        {
            List<Position> positions = new List<Position>();
            foreach (String s in data.Keys)
            {
                Position pos = parsePosition(s);
                if (!positions.Contains(pos))
                {
                    positions.Add(pos);
                }
            }
            return positions;
        }

        private static Position parsePosition(String key)
        {
            Position p = new Position();
            String[] s = key.Split(new char[] { ' ' });
            p.row = int.Parse(s[0]);
            p.col = int.Parse(s[1]);

            return p;
        }
        private static void PlaceAndResizeChart(ExcelChart chart, string action, TickData.DataType type)
        {
            int row, width, col, height;
            col = row = 0;
            width = 800; height = 400;
            int colStep = 13;
            int rowStep = 20;
            int rowOffsetTrials = 60;
            if (type == TickData.DataType.QValue)
            {
                switch (action)
                {
                    case "left":
                        row = 0;
                        col = 0;
                        break;
                    case "right":
                        row = 0;
                        col = colStep;
                        break;
                    case "down":
                        row = rowStep;
                        col = 0;
                        break;
                    case "up":
                        row = rowStep;
                        col = colStep;
                        break;
                    case "null":
                        row = rowStep * 2;
                        col = 0;
                        break;
                    case "comparison":
                        width = (int)(width * 1.3);
                        row = rowStep * 2;
                        col = colStep;
                        break;
                }
            }
            else if (type == TickData.DataType.Trial)
            {
                switch (action)
                {
                    case "left":
                        row = rowOffsetTrials;
                        col = 0;
                        break;
                    case "right":
                        row = rowOffsetTrials;
                        col = colStep;
                        break;
                    case "down":
                        row = rowOffsetTrials + rowStep;
                        col = 0;
                        break;
                    case "up":
                        row = rowOffsetTrials + rowStep;
                        col = colStep;
                        break;
                    case "null":
                        row = rowOffsetTrials + rowStep * 2;
                        col = 0;
                        break;
                    case "comparison":
                        width = 600;
                        row = rowOffsetTrials + rowStep * 2;
                        col = colStep;
                        break;
                }
            }
            chart.SetPosition(row, 0, col, 0);
            chart.SetSize(width, height);
        }
    }



    
}