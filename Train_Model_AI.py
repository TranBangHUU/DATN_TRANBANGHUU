import tensorflow as tf
from tensorflow.keras.applications import MobileNet
from tensorflow.keras.layers import GlobalAveragePooling2D, Dense, Dropout
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.callbacks import ModelCheckpoint
from tensorflow.keras.optimizers import Adam
from sklearn.utils import class_weight
import numpy as np
import os

N_CLASS_THUC_TE = 30 
IMG_SIZE = (224, 224)
BATCH_SIZE = 32
n_epochs = 30 
LEARNING_RATE = 1e-4

data_folder = "/mnt/e/CodeBuild/Dataset/train" 
MODEL_SAVE_DIR = "models"
# -------------------------------------------------------------

if not os.path.exists(MODEL_SAVE_DIR):
    os.makedirs(MODEL_SAVE_DIR)

#BUILD MODEL
def get_model(n_classes):
    base_model = MobileNet(include_top=False, weights="imagenet", input_shape=IMG_SIZE + (3,))

    for layer in base_model.layers:
        layer.trainable = False
    
    for layer in base_model.layers[-50:]: 
        if not isinstance(layer, tf.keras.layers.BatchNormalization):
            layer.trainable = True

    # Tạo model
    x = base_model.output
    x = GlobalAveragePooling2D()(x)
    x = Dense(256, activation='relu')(x)
    x = Dropout(0.5)(x)
    outs = Dense(n_classes, activation='softmax')(x)

    model = Model(inputs=base_model.inputs, outputs=outs)
    return model

train_datagen = ImageDataGenerator(
    preprocessing_function=tf.keras.applications.mobilenet.preprocess_input,
    rotation_range=20, width_shift_range=0.2, height_shift_range=0.2,
    shear_range=0.2, zoom_range=0.2, horizontal_flip=True, vertical_flip=False,
    validation_split=0.2
)

train_generator = train_datagen.flow_from_directory(
    data_folder, target_size=IMG_SIZE, batch_size=BATCH_SIZE,
    class_mode='categorical', subset='training'
)

validation_generator = train_datagen.flow_from_directory(
    data_folder, target_size=IMG_SIZE, batch_size=BATCH_SIZE,
    class_mode='categorical', subset='validation'
)


class_indices = train_generator.classes
class_weights = class_weight.compute_class_weight(
    'balanced',
    classes=np.unique(class_indices),
    y=class_indices
)
class_weights_dict = dict(enumerate(class_weights))
print("\nTrọng số lớp (Class Weights) đã được tính toán để cân bằng dữ liệu.")


classes = train_generator.class_indices
print("Các lớp và chỉ số tương ứng:", classes)
if len(classes) != N_CLASS_THUC_TE:
    print(f" CẢNH BÁO: Số lớp thực tế tìm thấy ({len(classes)}) không khớp với N_CLASS_THUC_TE ({N_CLASS_THUC_TE})!")
    print("Vui lòng sửa lại biến N_CLASS_THUC_TE cho đúng.")
    N_CLASS_THUC_TE = len(classes) 
     
model = get_model(N_CLASS_THUC_TE)

optimizer = Adam(learning_rate=LEARNING_RATE) 
model.compile(optimizer=optimizer, loss='categorical_crossentropy', metrics=['accuracy'])
model.summary()

# Checkpoint
checkpoint = ModelCheckpoint(MODEL_SAVE_DIR + '/best.keras', 
                             monitor='val_loss', save_best_only=True, mode='auto')
callback_list = [checkpoint]

step_train = train_generator.n // BATCH_SIZE
step_val = validation_generator.n // BATCH_SIZE

print(f"\nBắt đầu Huấn luyện với {n_epochs} Epochs...")
history = model.fit(
    train_generator,
    steps_per_epoch=step_train,
    validation_data=validation_generator,
    validation_steps=step_val,
    callbacks=callback_list,
    epochs=n_epochs,
    class_weight=class_weights_dict
)

model.save(MODEL_SAVE_DIR + '/model_final.h5')
print("\nQuá trình huấn luyện đã hoàn tất.")
print(f"Model cuối cùng được lưu thành '{MODEL_SAVE_DIR}/model_final.h5'")
print(f"Model tốt nhất được lưu thành '{MODEL_SAVE_DIR}/best.keras'")