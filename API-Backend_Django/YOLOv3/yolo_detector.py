from __future__ import division
import time
import torch 
import torch.nn as nn
from torch.autograd import Variable
import numpy as np
import cv2 
from . import util
from . import darknet
from . import preprocess
import pandas as pd
import random 
import pickle as pkl
import os

def get_test_input(input_dim, CUDA):
    img = cv2.imread("imgs/messi.jpg")
    img = cv2.resize(img, (input_dim, input_dim)) 
    img_ =  img[:,:,::-1].transpose((2,0,1))
    img_ = img_[np.newaxis,:,:,:]/255.0
    img_ = torch.from_numpy(img_).float()
    img_ = Variable(img_)
    if CUDA:
        img_ = img_.cuda()
    return img_


def prep_image(img, inp_dim):
    orig_im = img
    dim = orig_im.shape[1], orig_im.shape[0]
    img = cv2.resize(orig_im, (inp_dim, inp_dim))
    img_ = img[:,:,::-1].transpose((2,0,1)).copy()
    img_ = torch.from_numpy(img_).float().div(255.0).unsqueeze(0)
    return img_, orig_im, dim



def write(x, img):
    c1 = tuple(x[1:3].int())
    c2 = tuple(x[3:5].int())
    cls = int(x[-1])
    label = "{0}".format(classes[cls])
    color = random.choice(colors)
    cv2.rectangle(img, c1, c2,color, 10)
    t_size = cv2.getTextSize(label, cv2.FONT_HERSHEY_PLAIN, 1 , 1)[0]
    c2 = c1[0] + t_size[0] + 3, c1[1] + t_size[1] + 4
    cv2.rectangle(img, c1, c2,color, -1)
    cv2.putText(img, label, (c1[0], c1[1] + t_size[1] + 4), cv2.FONT_HERSHEY_PLAIN, 4, [225,255,255], 3);
    return img


cfgfile = os.path.join(os.path.dirname(os.path.realpath(__file__)),"yolov3.cfg")
weightsfile = os.path.join(os.path.dirname(os.path.realpath(__file__)),"yolov3.weights")
num_classes = 80

confidence = float(0.3)
nms_thesh = float(0.3)
start = 0
CUDA = torch.cuda.is_available()

num_classes = 80
bbox_attrs = 5 + num_classes

model = darknet.Darknet(cfgfile)
model.load_weights(weightsfile)

model.net_info["height"] = 512
inp_dim = int(model.net_info["height"])

assert inp_dim % 32 == 0 
assert inp_dim > 32

if CUDA:
    model.cuda()
        
model.eval()

frames = 0
classes = util.load_classes(os.path.join(os.path.dirname(os.path.realpath(__file__)),'data/coco.names'))
colors = pkl.load(open(os.path.join(os.path.dirname(os.path.realpath(__file__)),"pallete"), "rb"))

       
def Object_Detection(image):
    
    frame = image.copy()    
        
    img, orig_im, dim = prep_image(frame, inp_dim)
    
    im_dim = torch.FloatTensor(dim).repeat(1,2)                        
    
    if CUDA:
        im_dim = im_dim.cuda()
        img = img.cuda()
    
    output = model(Variable(img), CUDA)
    output = util.write_results(output, confidence, num_classes, nms = True, nms_conf = nms_thesh)

    output[:,1:5] = torch.clamp(output[:,1:5], 0.0, float(inp_dim))/inp_dim
    output[:,[1,3]] *= frame.shape[1]
    output[:,[2,4]] *= frame.shape[0]
    
    list(map(lambda x: write(x, orig_im), output))
    return orig_im
   
