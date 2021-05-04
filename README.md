# VolumePrediction
# Get Started

VolumePrediction contains code and datasets for ICWSM 2021 publication:

* **He, L., Shen, C., Mukherjee, A., Vucetic, S., Dragut, E., Cannot Predict Comment Volume of a News Article before (a few) Users Read It, ICWSM, 2021**

To run the code, the following environment is required:
* numpy>=1.13.1
* scipy>=0.19.1
* scikit-learn>=0.19.0

# Run the experiment for volume prediction. 
``
python volumePrediction.py
``

Calculate the R^2 and MAE with different feature groups and ML algorithms.
Feature groups: all features, comment features, article features, rate.
ML algorithms: Random Forest, SVM, MLP, Linear Regression.


# Analyze rate models.
``
python rateAnalysis.py
``

Show the Slope and Intercept of the rate model in each dataset.


# Dataset
Collect articles and comments from 6 news outlets: Daily Mail, Fox News, the Guardian, New York Times, Wall Street Journal, and Washington Post.

Dataset to run the experiments: DailyMail-1.csv, Fox-1.csv, Guardian-1.csv, NYTimes-1.csv, WSJ-1.csv, WSP-1.csv.

Folder Article_Comment gives the article url, id of first 10 comments, and comment volume from each outlet.

