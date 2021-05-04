# -*- coding: utf-8 -*-
"""
Created on Mon Apr 13 11:18:10 2020

@author: lihong

Predict the comment volumn
"""

import numpy as np
# import matplotlib.pyplot as plt
# from random import randint
import csv
from sklearn.model_selection import KFold
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.neural_network import MLPRegressor
from sklearn.svm import SVR
# from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error
# from sklearn import preprocessing
# from scipy import stats
# import matplotlib.pyplot as plt
# from scipy.stats import lognorm, kstest
# import json

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
  
  

  

def evaluateMdl(X_train, X_test, Y_train, Y_test):
    # #Linear regression
    # regr = LinearRegression()
    #Random Forest
    regr = RandomForestRegressor(n_estimators=200)    
    # #FeedForward Neural Network: MLP
    # regr = MLPRegressor(hidden_layer_sizes=(20,), max_iter=200, learning_rate_init=0.01)
    # #SVR
    # regr = SVR(kernel='rbf', C=10, epsilon=0.1)
    
    #Reshape if only one feature
    if X_train.shape[0] == X_train.size:
        X_train = X_train.reshape(-1,1)
        X_test = X_test.reshape(-1,1)    
    regr = regr.fit(X_train, Y_train)
    
    Y_pred = regr.predict(X_test)
    Y_dif = np.subtract(Y_test, Y_pred)
    
    
#    print(regr.score(X_test, Y_test))       
#    print('Variance score: %.3f' % r2_score(Y_test, Y_pred))   
#    print('Mean absolute error: %.3f' % mean_absolute_error(Y_test, Y_pred))   
#    print('Mean squared error: %.3f' % mean_squared_error(Y_test, Y_pred))
    
    return r2_score(Y_test, Y_pred), mean_absolute_error(Y_test, Y_pred)
#    return r2_score(Y_test, Y_pred), mean_absolute_error(Y_test, Y_pred), Y_dif #for checking articleID
  
def regressionPerf(wspX, wspY):
    
    avgWSPR2 = 0
    avgWSPL1 = 0
    #split
    splitNum = 5
    kf = KFold(n_splits = splitNum, shuffle=True)
    #in each split
    ite = 0
    for train_index, test_index in kf.split(wspX):  
        ite = ite + 1
        wspX_train, wspX_test = wspX[train_index], wspX[test_index]
        wspY_train, wspY_test = wspY[train_index], wspY[test_index]   
#        #keep articleID for checking
#        wspX_train_ID = wspX_train[:,0]
#        wspX_test_ID = wspX_test[:,0]
#        wspX_train = wspX_train[:,1]
#        wspX_test = wspX_test[:,1]
        
        #print('\n------ In round '+str(ite)+' ------')       
        WSPR2, WSPL1 = evaluateMdl(wspX_train, wspX_test, wspY_train, wspY_test)
#        WSPR2, WSPL1, Y_dif = evaluateMdl(wspX_train, wspX_test, wspY_train, wspY_test)#for checking articleID
#        WSPR2, WSPL1 = evaluateMdl(wspX_train, wspX_train, wspY_train, wspY_train) #same train test
#        WSPR2, WSPL1 = meanPredict(wspX_train, wspX_test, wspY_train, wspY_test) #mean on training to predict test
        avgWSPR2 = avgWSPR2 + WSPR2
        avgWSPL1 = avgWSPL1 + WSPL1
        #print('WSP, R2: %.3f, L1: %.3f ' % (WSPR2, WSPL1))        
    #calculate the performace in average
    avgWSPR2 = avgWSPR2 / splitNum
    avgWSPL1 = avgWSPL1 / splitNum
    
    return avgWSPR2, avgWSPL1



def prediction():
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
    
    
#    #remove comment avg sentiment score feature
#    featureSize =  np.size(wspX, 1)
#    wspX = np.delete(wspX, featureSize-1, 1)
#    dmX = np.delete(dmX, featureSize-1, 1)
#    wsjX = np.delete(wsjX, featureSize-1, 1)
#    foxX = np.delete(foxX, featureSize-1, 1)
#    gdX = np.delete(gdX, featureSize-1, 1)
#    nytX = np.delete(nytX, featureSize-1, 1)
    
#    #remove article sentiment score feature
#    featureSize =  np.size(wspX, 1)
#    wspX = np.delete(wspX, featureSize-2, 1)
#    dmX = np.delete(dmX, featureSize-2, 1)
#    wsjX = np.delete(wsjX, featureSize-2, 1)
#    foxX = np.delete(foxX, featureSize-2, 1)
#    gdX = np.delete(gdX, featureSize-2, 1)
#    nytX = np.delete(nytX, featureSize-2, 1)
    
#    #remove 2 sentiment score features = previous features
#    featureSize =  np.size(wspX, 1)
#    wspX = np.delete(wspX, [featureSize-2, featureSize-1], 1)
#    dmX = np.delete(dmX, [featureSize-2, featureSize-1], 1)
#    wsjX = np.delete(wsjX, [featureSize-2, featureSize-1], 1)
#    foxX = np.delete(foxX, [featureSize-2, featureSize-1], 1)
#    gdX = np.delete(gdX, [featureSize-2, featureSize-1], 1)
#    nytX = np.delete(nytX, [featureSize-2, featureSize-1], 1)
    
#    #use rate only
#    wspX = wspX[:, 1]
#    dmX = dmX[:, 1]
#    wsjX = wsjX[:, 1]
#    foxX = foxX[:, 1]
#    gdX = gdX[:, 1]
#    nytX = nytX[:, 1]   
        
#    #use comment feature only
#    wspX = wspX[:, [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 22]]
#    dmX = dmX[:, [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 22]]
#    wsjX = wsjX[:, [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 22]]
#    foxX = foxX[:, [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 22]]
#    gdX = gdX[:, [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 22]]
#    nytX = nytX[:, [1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 22]]
    
#    #use article feature only
#    wspX = wspX[:, [2, 3, 4, 18, 19, 23, 24, 25, 26]]
#    dmX = dmX[:, [2, 3, 4, 18, 19, 23, 24, 25, 26]]
#    wsjX = wsjX[:, [2, 3, 4, 18, 19, 23, 24, 25, 26]]
#    foxX = foxX[:, [2, 3, 4, 18, 19, 23, 24, 25, 26]]
#    gdX = gdX[:, [2, 3, 4, 18, 19, 23, 24, 25, 26]]
#    nytX = nytX[:, [2, 3, 4, 18, 19, 23, 24, 25, 26]]
    
    #data for all outlet
    allX = np.concatenate((wspX, dmX, wsjX, foxX, gdX, nytX))
    allY = np.concatenate((wspY, dmY, wsjY, foxY, gdY, nytY))
    
    # print(len(allY))
            
#    #normalization: mean=0, std=1
#    allX = preprocessing.scale(allX)
#    wspX = preprocessing.scale(wspX)
#    dmX = preprocessing.scale(dmX)
#    wsjX = preprocessing.scale(wsjX)
#    foxX = preprocessing.scale(foxX)
#    gdX = preprocessing.scale(gdX)
#    nytX = preprocessing.scale(nytX)
    
    
    
    #do regression and evaluation
    avgALLR2, avgALLL1 = regressionPerf(allX, allY)
    print('In All, R2: %.3f, L1: %.3f ' % (avgALLR2, avgALLL1))
    print('Using each outlet for training:')
    avgWSPR2, avgWSPL1 = regressionPerf(wspX, wspY)
    print('In WSP, R2: %.3f, L1: %.3f ' % (avgWSPR2, avgWSPL1))
    avgDMR2, avgDML1 = regressionPerf(dmX, dmY)
    print('In DM, R2: %.3f, L1: %.3f ' % (avgDMR2, avgDML1))
    avgWSJR2, avgWSJL1 = regressionPerf(wsjX, wsjY)
    print('In WSJ, R2: %.3f, L1: %.3f ' % (avgWSJR2, avgWSJL1))
    avgFOXR2, avgFOXL1 = regressionPerf(foxX, foxY)
    print('In FOX, R2: %.3f, L1: %.3f ' % (avgFOXR2, avgFOXL1))
    avgGDR2, avgGDL1 = regressionPerf(gdX, gdY)
    print('In GD, R2: %.3f, L1: %.3f ' % (avgGDR2, avgGDL1))
    avgNYTR2, avgNYTL1 = regressionPerf(nytX, nytY)
    print('In NYT, R2: %.3f, L1: %.3f ' % (avgNYTR2, avgNYTL1))    
    

def main():
    prediction()
    
    
    
if __name__=='__main__':
    main()
