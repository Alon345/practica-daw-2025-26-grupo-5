import express from 'express';
import * as Service from './service.js';
import multer from 'multer';
import fs from 'node:fs/promises';

const UPLOADS_FOLDER = 'uploads';
const DEMO_FOLDER = 'demo';
const router=express.Router();

const upload = multer({ dest: UPLOADS_FOLDER });
//Copia de la carpeta demo images en la carpeta upload folder
fs.cp(DEMO_FOLDER, UPLOADS_FOLDER,{ recursive: true, force: true });