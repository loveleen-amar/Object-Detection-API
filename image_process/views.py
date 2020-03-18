from django.shortcuts import render
from django.http import  HttpResponse, HttpResponseRedirect
import cv2
import numpy as np
from PIL import Image
from django.views.decorators.csrf import csrf_exempt
from YOLOv3 import yolo_detector
# Create your views here.
@csrf_exempt

def index(request):
    if request.method == 'GET':
        return render(request,'image_process/form.html')
    if request.method == 'POST':
        content = 'image/png'
        response = HttpResponse(content_type=content)
        image = request.FILES['image'].read()
        image = np.asarray(bytearray(image), dtype="uint8")
        image = cv2.imdecode(image, cv2.IMREAD_COLOR)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        image = yolo_detector.Object_Detection(image)
        final = Image.fromarray(image)
        final.save(response,"png")
        return response
