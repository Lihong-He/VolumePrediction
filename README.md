# VolumePrediction
# Abstract

We study the problem of predicting the total number of user comments a news article will receive in this project.
Our main insight is that the early dynamics of user comments contribute the most to an accurate prediction, while news article specific factors have surprisingly little influence. This appears to be an interesting and understudied phenomenon: collective social behavior at a news outlet shapes user response and may even downplay the content of an article.
We compile and analyze a large number of features, both old and novel from literature. 
The features span a broad spectrum of facets including news article and comment contents, temporal dynamics, sentiment/linguistic features, and user behaviors. 
We show that the early arrival rate of comments is the best indicator of the eventual number of comments. We conduct an in-depth analysis of this feature across several dimensions, such as news outlets and news article categories.
We show that the relationship between the early rate and the final number of comments as well as the prediction accuracy vary considerably across news outlets and news article categories (e.g., politics, sports, or health).

# Get Started

VolumePrediction contains code and datasets for our ICWSM 2021 publication:

[**Cannot Predict Comment Volume of a News Article before (a few) Users Read It**](https://arxiv.org/abs/2008.06414)

To run the code, the following environment is required:
* numpy>=1.13.1
* scipy>=0.19.1
* scikit-learn>=0.19.0

# Run the experiment for volume prediction
``
python volumePrediction.py
``

Calculate the R^2 and MAE with different feature groups and ML algorithms.
Feature groups: all features, comment features, article features, rate.
ML algorithms: Random Forest, SVM, MLP, Linear Regression.


# In-depth study of rate models
``
python rateAnalysis.py
``

Show the Slope and Intercept of the rate model in each dataset.


# Dataset
Collect articles and comments from 6 news outlets: Daily Mail, Fox News, the Guardian, New York Times, Wall Street Journal, and Washington Post.

Dataset to run the experiments: DailyMail-1.csv, Fox-1.csv, Guardian-1.csv, NYTimes-1.csv, WSJ-1.csv, WSP-1.csv.

Folder Article_Comment gives the article url, id of first 10 comments, and comment volume from each outlet.

