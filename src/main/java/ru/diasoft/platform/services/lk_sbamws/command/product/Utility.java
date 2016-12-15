package ru.diasoft.platform.services.lk_sbamws.command.product;


import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;




/**
 * Данный класс содержит всякие полезности, которые могут быть использованы в
 * разных частях проекта
 * 

 * 
 */
public class Utility {

    public static String get_timestamp(){

        Date date = new Date();
        return
                String.format("%tY.%tm.%td %tH:%tM:%tS %tz" , date, date, date,date, date, date,date);
    }

    /**
     * Стремная константа, применяемая для запроса состояния из АРМ настройщика.
     */
    public static String PAPONOV_REQUEST = "REQUEST";

    /**
     * метод переводит Image в массив байт. Если не удалось генерит исключение
     * 
     * @param image
     * @return
     *
     */
    public static byte[] converterBytesFromImage(Image image)
			throws  Exception {



	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	int width = image.getWidth(null);
	int height = image.getHeight(null);
	try {
	    BufferedImage bi = new BufferedImage(width, height,
		    BufferedImage.TYPE_INT_RGB);
	    // чтобы сохранить изображение - его необходимо отрисовать!
	    Graphics2D g2d = bi.createGraphics();

	    g2d.drawImage(image, 0, 0, null);
	    try {
		ImageIO.write(bi, "JPEG", baos);
	    } catch (IOException e) {
		throw new  Exception(e.getMessage(), e);
	    }
	    return baos.toByteArray();
	} catch (Throwable t) {
	    throw new Exception("Не хватает памяти для картинки");
	}
    }

    /**
     * Метод возвращает имя локальной машины.
     * 
     * @return Имя локальной машины.
     * @throws UnknownHostException
     */
    public static String getLocalHostName() throws UnknownHostException {
	InetAddress add;
	add = InetAddress.getLocalHost();
	return add.getHostName();
    }

    /**
     * Метод позволяет получить "безопасный" объект - избавляемся
     * сериализацией-десериализацией от ссылок
     * 
     * @param object
     *            - любой объект
     * @return
     * @throws
     */
    static public Object securityObject(Serializable object) throws Exception
	{

	return Utility.deserializeObject(Utility.serializeObject(object));

    }

    /**
     * Сериализация объекта
     * 
     * @param object
     *            - любой объект (ArrayList тоже можно)
     * @return
     * @throws
     */
    static public byte[] serializeObject(Serializable object) throws Exception {

	try {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    ObjectOutputStream out = new ObjectOutputStream(bout);
	    out.writeObject(object);
	    out.flush();
	    out.close();

	    return bout.toByteArray();

	} catch (IOException e) {
	    throw new Exception(
		    "Не удалось сериализовать объект в байты", e);
	}
    }

    /**
     * Десериализация объекта
     * 
     * @param bout
     * @return
     * @throws
     */
    static public Object deserializeObject(byte[] bout) throws Exception {

	try {

	    ObjectInputStream in = new ObjectInputStream(
		    new ByteArrayInputStream(bout));

	    Object object = in.readObject();
	    in.close();

	    return object;

	} catch (IOException e) {
	    throw new Exception(
		    "Не удалось десериализовать байты в объект", e);
	} catch (ClassNotFoundException e) {
	    throw new Exception(
		    "Не удалось десериализовать байты в объект", e);
	}

    }

    /**
     * Метод получает массив байт файла !!!!!!Внимание метод не работает с
     * файлами из упакованных сборок JAR
     * 
     * @param f
     *            - файл
     * @return - массив байт файла или null в случае ошибки
     */
    static public byte[] getFileBytes(File f) {
	InputStream is = null;
	try {
	    is = new FileInputStream(f);
	} catch (FileNotFoundException e) {
	    return null;
	}
	long length = f.length();
	if (length <= 0) {
	    try {
		is.close();
		return null;
	    } catch (IOException e) {
		return null;
	    }
	}
	byte[] result = new byte[(int) length];
	int offset = 0;
	int numRead = 0;
	while (offset < result.length) {
	    try {
		numRead = is.read(result, offset, result.length - offset);
	    } catch (IOException e) {
		try {
		    is.close();
		    return null;
		} catch (IOException e1) {
		    return null;
		}
	    }
	    if (numRead < 0) {
		break;
	    }
	    offset += numRead;
	}
	try {
	    is.close();
	} catch (IOException e) {
	    return null;
	}
	return result;
    }

    /**
     * 
     * @param rootClass
     *            - класс в подпапке расположения которого находится ресурс
     * @param resourceFolderName
     *            - имя подпапки расположения - должно оканчиваться символом "/"
     * @param fileName
     *            - простое имя(без пути) файла
     * @return
     */
    static public byte[] getFileBytes(Class<?> rootClass,
	    String resourceFolderName, String fileName) {
	InputStream inpStream = rootClass
		.getResourceAsStream(resourceFolderName + fileName);
	if (inpStream == null) {
	    return null;
	}
	ArrayList<Byte> listByte = new ArrayList<Byte>();
	for (;;) {
	    try {
		int intByte = inpStream.read();
		if (intByte < 0) {
		    break;
		}
		listByte.add(new Byte((byte) intByte));
	    } catch (IOException e) {
		try {
		    inpStream.close();
		} catch (IOException e1) {
		    return null;
		}
		return null;
	    }
	}
	try {
	    inpStream.close();
	} catch (IOException e) {
	    return null;
	}
	int iMax = listByte.size();
	if (iMax <= 0) {
	    return new byte[0];
	}
	byte[] result = new byte[iMax];
	int i = 0;
	for (Byte cycleByte : listByte) {
	    result[i] = cycleByte.byteValue();
	    i++;
	}
	return result;
    }

    /**
     * Метод создает ImageIcon из массива байт
     * 
     * @param imageArray
     *            - массив байт
     * @return ImageIcon или null в случае ошибки
     */
    static public ImageIcon makeImageIconFromArray(byte[] imageArray) {
	if (imageArray == null) {
	    return null;
	}
	if (imageArray.length <= 0) {
	    return null;
	}
	return new ImageIcon(imageArray);
    }

    /**
     * Метод конвертирует список в массив
     * 
     * @param list
     * @return
     */
    public static byte[] convert(ArrayList<Byte> list) {
	byte[] result = new byte[list.size()];
	for (int i = 0; i < list.size(); i++) {
	    result[i] = list.get(i).byteValue();
	}
	return result;
    }


    /**
     * Метод переводит массив байтов в строку для HandKey
     * 
     * @param arByte
     * @return
     * @throws IllegalArgumentException
     */
    public static String hkBytesToStr(byte[] arByte)
	    throws IllegalArgumentException {
	if (arByte == null) {
	    throw new IllegalArgumentException("Нет массива байт!!!");
	}
	if (arByte.length <= 0) {
	    throw new IllegalArgumentException("Массива байт пуст!!!");
	}
	String result = "";
	for (byte bait : arByte) {
	    result += String.format("%02x ", bait);
	}
	return result;
    }

    /**
     * Метод возвращает строку с hex кодами из массива байт.
     * 
     * @param message
     * @return
     */
    public static synchronized String getHexString(byte[] message) {
	StringBuilder builder = new StringBuilder();
	for (int n = 0; n < message.length; n++) {
	    builder.append(String.format("%1$02X", message[n]) + " ");
	}
	return builder.toString();
    }

    /**
     * Метод возвращает строку с hex кодами из массива байт.
     * 
     * @param message
     * @return
     */
    public static synchronized String getHexString(List<Byte> message) {
	StringBuilder builder = new StringBuilder();
	for (Byte item : message) {
	    builder.append(String.format("%1$02X", item.byteValue()) + " ");
	}
	return builder.toString();
    }

    /**
     * Метод переводит строку в массив байтов для HandKey
     * 
     * @param arStr
     * @return
     * @throws IllegalArgumentException
     */
    public static byte[] hkStrToBytes(String arStr)
	    throws IllegalArgumentException {
	if (arStr == null) {
	    throw new IllegalArgumentException("Нет строки байт!!!");
	}
	if (arStr.length() <= 0) {
	    throw new IllegalArgumentException("Строка байт пуста!!!");
	}
	int triadaRest = arStr.length() % 3;
	if (triadaRest != 0) {
	    throw new IllegalArgumentException(
		    "Длина строки некратна трём - неправильный формат!!!");
	}
	int numTriades = arStr.length() / 3;
	ArrayList<Byte> arBytes = new ArrayList<Byte>();
	for (int i = 0; i < numTriades; i++) {
	    String strTmp = arStr.substring(i * 3, i * 3 + 2);
	    strTmp = strTmp.trim();
	    short baitShort = Short.parseShort(strTmp, 16);
	    byte bait = (byte) baitShort;
	    arBytes.add(new Byte(bait));
	}
	byte[] arbytes = new byte[arBytes.size()];
	int index = 0;
	for (Byte iByte : arBytes) {
	    arbytes[index] = iByte.byteValue();
	    index++;
	}
	return arbytes;
    }

    /**
     * Метод опредяляет тип операционной системы компьютера
     * 
     * @return 1 - Windows , 2 - Linux , 0 - Не смогли определить
     */
    public static int getTypeOS() {
	String osName = System.getProperty("os.name");
	if (osName == null) {
	    return 0;
	}
	if (osName.length() <= 0) {
	    return 0;
	}
	osName = osName.toLowerCase();
	if (osName.indexOf("windows") >= 0) {
	    return 1;
	}
	if (osName.indexOf("linux") >= 0) {
	    return 2;
	}
	return 0;
    }

    /**
     * Метод выставляет временную зону (0 - Гринвич, 4 - Москва)
     * 
     * @param number
     *            - разница в часах между временем в Гринвиче и в текущей
     *            местности
     */
    public static void setTimeZoneByNumber(int number) {
	String res = null;
	if (number == 0) {
	    res = "GMT";
	} else if (number > 0) {
	    res = String.format("GMT+%d:00", number);
	} else {// number <0
	    int negNumber = -number;
	    res = String.format("GMT-%d:00", negNumber);
	}
	TimeZone.setDefault(TimeZone.getTimeZone(res));
    }



    /**
     * Метод принимает на вход строку, ищет заглавные буквы и возвращает строку
     * с заглавными буквами
     * 
     * @param str
     * @return
     */
    public static String getCapitalLettersFromString(String str) {
	Pattern pattern = Pattern.compile("[A-Z]");
	Matcher m = pattern.matcher(str);
	String newstr = "";
	while (m.find()) {
	    newstr = newstr + m.group();
	}
	return newstr;
    }

    /**
     * Метод принимает на вход строку, ищет заглавные русские буквы и возвращает
     * строку с заглавными буквами
     * 
     * @param str
     * @return
     */
    public static String getCapitalLettersRusFromString(String str) {
	Pattern pattern = Pattern.compile("[А-Я]");
	Matcher m = pattern.matcher(str);
	String newstr = "";
	while (m.find()) {
	    newstr = newstr + m.group();
	}
	return newstr;
    }

    /**
     * Метод принимает строку, ищет маленькие буквы и возвращает строку с
     * маленькими буквами
     * 
     * @param str
     * @return
     */
    public static String getLowerCaseLettersFromString(String str) {
	Pattern pattern = Pattern.compile("[a-z]");
	Matcher m = pattern.matcher(str);
	String newstr = "";
	while (m.find()) {
	    newstr = newstr + m.group();
	}
	return newstr;
    }

    /**
     * Метод принимает строку, ищет маленькие русские буквы и возвращает строку
     * с маленькими буквами
     * 
     * @param str
     * @return
     */
    public static String getLowerCaseLettersRusFromString(String str) {
	Pattern pattern = Pattern.compile("[а-я]");
	Matcher m = pattern.matcher(str);
	String newstr = "";
	while (m.find()) {
	    newstr = newstr + m.group();
	}
	return newstr;
    }

    /**
     * Метод принимает на вход строку ищет цифры и возвращает строку с цифрами
     * 
     * @param str
     * @return
     */
    public static String getFiguresFromString(String str) {
	Pattern pattern = Pattern.compile("[0-9]");
	Matcher m = pattern.matcher(str);
	String newstr = "";
	while (m.find()) {
	    newstr = newstr + m.group();
	}
	return newstr;
    }

    /**
     * Метод по классу находящемуся в запущенном jar-файле находит каталог
     * содержащий этот jar-файл. Только для ОС Windows!!! Позволяет проверить
     * откуда выполняется программа, если возвращает null = значит не из
     * jar-файла
     * 
     * @param classInJar
     *            - любой класс из jar-файла
     * @return
     */
    public static String getPathToExecJar(Class<?> classInJar) {
	String classNameWithExt = classInJar.getName() + ".class";
	java.net.URL url = Thread.currentThread().getContextClassLoader()
		.getResource(classNameWithExt);
	if (url == null) {
	    return null;
	}
	String fullPath = url.getPath();
	if ((fullPath == null) || (fullPath.length() <= 0)) {
	    return null;
	}
	String jarDet = "file:/";
	int index1 = fullPath.indexOf(jarDet);
	String pathL1 = null;
	if (index1 < 0) {
	    pathL1 = fullPath.substring(1);

	} else {
	    pathL1 = fullPath.substring(jarDet.length());
	}
	if ((pathL1 == null) || (pathL1.length() <= 0)) {
	    return null;
	}
	String pathL2 = pathL1.replace('/', '\\');
	String detJarStr = ".jar";
	int findDet = pathL2.indexOf(detJarStr);
	if (findDet < 0) {
	    findDet = pathL2.indexOf(classNameWithExt);
	    if (findDet < 0) {
		return null;
	    }
	    return pathL2.substring(0, findDet);
	}
	String pathL3 = pathL2.substring(0, findDet);
	if ((pathL3 == null) || (pathL3.length() <= 0)) {
	    return null;
	}
	int iMax = pathL3.length() - 1;
	int iDetJF = -1;
	for (int i = iMax; i >= 0; i--) {
	    byte curByte = (byte) pathL3.charAt(i);
	    if (curByte == 0x5c) {
		iDetJF = i;
		break;
	    }
	}
	if (iDetJF < 0) {
	    return null;
	}
	return pathL3.substring(0, iDetJF + 1);
    }

    public static String byte13Rechange(String oldCode) {
	if (oldCode == null) {
	    return null;
	}
	if (oldCode.length() != 6) {
	    return oldCode;
	}
	String byte1 = oldCode.substring(0, 2);
	String byte2 = oldCode.substring(2, 4);
	String byte3 = oldCode.substring(4, 6);
	return byte3 + byte2 + byte1;
    }

}
