# -*- coding: utf-8 -*-
"""
Created on Fri Apr  3 13:48:12 2020

@author: lihong

Analyze rate to volume prediction

"""

import numpy as np
import csv
from sklearn.ensemble import RandomForestRegressor
from scipy import stats
#from matplotlib import pyplot as plt


def readCSVfile(csvFile):   
    dataMat = []  
    csvReader = csv.reader(open(csvFile, encoding='utf-8'), delimiter=',')
    for csvRow in csvReader: 
        if csvRow[0] == 'ID' or csvRow[0] == 'DELAY':
            continue
        csvData = [float(x) for x in csvRow]
        dataMat.append(csvData)
    dataMat = np.asarray(dataMat)
    featureMat = dataMat[:, :len(dataMat[0])-1]
    labelArray = dataMat[:, len(dataMat[0])-1]
    return featureMat, labelArray
  
  
  

def dataRegression(X, Y):
#    #Linear regression
#    model = LinearRegression()
#    #Logistic Regression, error Unknown label type: 'continuous'
#    model = LogisticRegression()
    #Random Forest
    model = RandomForestRegressor(n_estimators=100, min_samples_leaf=5, min_samples_split=10)    
#    #FeedForward Neural Network: MLP
#    model = MLPRegressor(hidden_layer_sizes=(20,), max_iter=200, learning_rate_init=0.01)
#    #SVR
#    model = SVR(kernel='rbf', C=10, epsilon=0.1)
    
    #Reshape if only one feature
    if X.shape[0] == X.size:
        X = X.reshape(-1,1) 
    model = model.fit(X, Y)
    # predict
    Y_pred = model.predict(X)
    
    #Remove val>120 (rate<0.0088) for interval
    idx120 = [i for i in range(len(X)) if X[i][0]<0.0088]
    cleanX = [j[0] for i, j in enumerate(X) if i not in idx120] 
    cleanY_pred = [j for i, j in enumerate(Y_pred) if i not in idx120]
    
    #Calculate a regression line
    logX = np.log10(cleanX)
#    listX = list(i[0] for i in logX)
#    listY = list(cleanY_pred)
    slope, intercept, r_value, p_value, std_err = stats.linregress(cleanX, cleanY_pred)
    
    return logX, cleanY_pred, slope, intercept, std_err
  
  
def main():
    folder = './dataset/'
    
    #true dataset for experiment
    wspCsvFile = folder + 'WSP-1.csv'
    dmCsvFile = folder + 'DailyMail-1.csv'
    wsjCsvFile = folder + 'WSJ-1.csv'
    foxCsvFile = folder + 'Fox-1.csv'
    gdCsvFile = folder + 'Guardian-1.csv'
    nytCsvFile = folder + 'NYTimes-1.csv'
    
    #get feature and label
    wspX, wspY = readCSVfile(wspCsvFile)
    dmX, dmY = readCSVfile(dmCsvFile)
    wsjX, wsjY = readCSVfile(wsjCsvFile)
    foxX, foxY = readCSVfile(foxCsvFile)
    gdX, gdY = readCSVfile(gdCsvFile)
    nytX, nytY = readCSVfile(nytCsvFile)   
    
    #use rate only
    wspX = wspX[:, 1]
    dmX = dmX[:, 1]
    wsjX = wsjX[:, 1]
    foxX = foxX[:, 1]
    gdX = gdX[:, 1]
    nytX = nytX[:, 1]       
    
    #data for all outlet
    allX = np.concatenate((wspX, dmX, wsjX, foxX, gdX, nytX))
    allY = np.concatenate((wspY, dmY, wsjY, foxY, gdY, nytY))
            
    
    
    #do regression and evaluation
    allLogX, allY_pred, allSlope, allIntercept, allStdError = dataRegression(allX, allY)
    print('In All, Slope: %.3f, Intercept: %.3f ' % (allSlope, allIntercept))
#    plt.scatter(allLogX, allY_pred, c='r')
    print('Using each outlet:')
    wspLogX, wspY_pred, wspSlope, wspIntercept, wspStdError = dataRegression(wspX, wspY)
    print('In WSP, Slope: %.3f, Intercept: %.3f ' % (wspSlope, wspIntercept))
    dmLogX, dmY_pred, dmSlope, dmIntercept, dmStdError = dataRegression(dmX, dmY)
    print('In DM, Slope: %.3f, Intercept: %.3f ' % (dmSlope, dmIntercept))
    wsjLogX, wsjY_pred, wsjSlope, wsjIntercept, wsjStdError = dataRegression(wsjX, wsjY)
    print('In WSJ, Slope: %.3f, Intercept: %.3f ' % (wsjSlope, wsjIntercept))
    foxLogX, foxY_pred, foxSlope, foxIntercept, foxStdError = dataRegression(foxX, foxY)
    print('In FOX, Slope: %.3f, Intercept: %.3f ' % (foxSlope, foxIntercept))
    gdLogX, gdY_pred, gdSlope, gdIntercept, gdStdError = dataRegression(gdX, gdY)
    print('In GD, Slope: %.3f, Intercept: %.3f ' % (gdSlope, gdIntercept))
    nytLogX, nytY_pred, nytSlope, nytIntercept, nytStdError = dataRegression(nytX, nytY)
    print('In NYT, Slope: %.3f, Intercept: %.3f ' % (nytSlope, nytIntercept))

    
if __name__=='__main__':
    main()