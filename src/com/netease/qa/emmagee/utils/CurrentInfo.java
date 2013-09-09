package com.netease.qa.emmagee.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Locale;

import android.os.Build;
import android.util.Log;

/**
 * Current info
 * 
 * @author andrewleo
 * 
 */
public class CurrentInfo {
	private static final String LOG_TAG = "Emmagee-CurrentInfo";
	static final String BUILD_MODEL = Build.MODEL.toLowerCase(Locale.ENGLISH);

	public Long getCurrentValue() {
		// FIXME gt-s5838 10
		File f = null;
		Log.d(LOG_TAG, BUILD_MODEL);
		Log.d(LOG_TAG, String.valueOf(android.os.Build.VERSION.SDK_INT));
		// Galaxy S4,oppo find(x907,云音乐),samgsung note2 16(云阅读，gt-n7100 only in charging。只能获取充电电流466mA always,放电时为0)
		if (BUILD_MODEL.contains("sgh-i337")
				|| BUILD_MODEL.contains("gt-i9505")
				|| BUILD_MODEL.contains("sch-i545")
				|| BUILD_MODEL.contains("find 5")
				|| BUILD_MODEL.contains("sgh-m919")
				|| BUILD_MODEL.contains("sgh-i537")
				|| BUILD_MODEL.contains("x907")
				|| BUILD_MODEL.contains("gt-n7100")) {
			f = new File("/sys/class/power_supply/battery/current_now");
			if (f.exists()) {
				return getCurrentValue(f, false);
			}
		}

		if (BUILD_MODEL.contains("cynus")) {
			f = new File(
					"/sys/devices/platform/mt6329-battery/FG_Battery_CurrentConsumption");
			if (f.exists()) {
				return getCurrentValue(f, false);
			}
		}
		// Samsung Galaxy Tab 2
		if (BUILD_MODEL.contains("gt-p31") || BUILD_MODEL.contains("gt-p51")) {
			f = new File("/sys/class/power_supply/battery/current_avg");
			if (f.exists()) {
				return getCurrentValue(f, false);
			}
		}
		// HTC One X
		if (BUILD_MODEL.contains("htc one x")) {
			f = new File("/sys/class/power_supply/battery/batt_attr_text");
			if (f.exists()) {
				Long value = getBattAttrValue(f, "I_MBAT", "I_MBAT");
				if (value != null)
					return value;
			}
		}
		// htc desire hd（相册） / desire z
		if (BUILD_MODEL.toLowerCase().contains("desire hd")
				|| Build.MODEL.toLowerCase().contains("desire z")) {
			f = new File("/sys/class/power_supply/battery/batt_current");
			if (f.exists())
				return getCurrentValue(f, false);
		}

		// galaxy note, galaxy s2(云音乐，SHV-E120L，only on charging)
		f = new File("/sys/class/power_supply/battery/batt_current_adc");
		if (f.exists())
			return getCurrentValue(f, false);

		// sony ericsson xperia x1
		f = new File(
				"/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/ds2746-battery/current_now");
		if (f.exists())
			return getCurrentValue(f, false);
		// xdandroid
		f = new File(
				"/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/battery/current_now");
		if (f.exists())
			return getCurrentValue(f, false);
		// droid eris，htc one V(lofter)
		f = new File("/sys/class/power_supply/battery/smem_text");
		if (f.exists())
			return getSMemValue();
		f = new File("/sys/class/power_supply/battery/batt_current");
		if (f.exists())
			return getCurrentValue(f, false);
		// nexus one,meizu（相册）
		f = new File("/sys/class/power_supply/battery/current_now");
		if (f.exists())
			return getCurrentValue(f, true);
		// samsung galaxy vibrant
		f = new File("/sys/class/power_supply/battery/batt_chg_current");
		if (f.exists())
			return getCurrentValue(f, false);
		// sony ericsson x10
		f = new File("/sys/class/power_supply/battery/charger_current");
		if (f.exists())
			return getCurrentValue(f, false);
		// bingo(xiaxing),acer V360（云音乐，on charging,cpu有问题,单核）
		f = new File("/sys/class/power_supply/battery/BatteryAverageCurrent");
		if (f.exists())
			return getCurrentValue(f, false);
		// moto milestone(10,lofter),Moto mb526(10,云音乐)
		f = new File(
				"/sys/devices/platform/cpcap_battery/power_supply/usb/current_now");
		if (f.exists())
			return getCurrentValue(f, false);

		return null;
	}

	/**
	 * 从smem_text文件中读取电流数据
	 * 
	 * @return
	 */
	public Long getSMemValue() {
		Log.d(LOG_TAG, "*** getSMemValue ***");
		boolean success = false;
		String text = null;
		try {
			FileReader fr = new FileReader(
					"/sys/class/power_supply/battery/smem_text");
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				if (line.contains("I_MBAT")) {
					text = line.substring(line.indexOf("I_MBAT: ") + 8);
					success = true;
					break;
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage());
			ex.printStackTrace();
		}
		Long value = null;
		if (success) {
			try {
				value = Long.parseLong(text);
			} catch (NumberFormatException nfe) {
				Log.e(LOG_TAG, nfe.getMessage());
				value = null;

			}
		}
		return value;
	}

	/**
	 * 从batt_attr中读取电流数据
	 * 
	 * @param f
	 * @param dischargeField
	 * @param chargeField
	 * @return
	 */
	public Long getBattAttrValue(File f, String dischargeField,
			String chargeField) {
		Log.d(LOG_TAG, "*** getBattAttrValue ***");
		String text = null;
		Long value = null;
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			final String chargeFieldHead = chargeField + ": ";
			final String dischargeFieldHead = dischargeField + ": ";
			while (line != null) {
				if (line.contains(chargeField)) {
					text = line.substring(line.indexOf(chargeFieldHead)
							+ chargeFieldHead.length());
					try {
						value = Long.parseLong(text);
						if (value != 0)
							break;
					} catch (NumberFormatException nfe) {
						Log.e(LOG_TAG, nfe.getMessage(), nfe);
					}
				}
				if (line.contains(dischargeField)) {
					text = line.substring(line.indexOf(dischargeFieldHead)
							+ dischargeFieldHead.length());
					try {
						value = (-1) * Math.abs(Long.parseLong(text));
					} catch (NumberFormatException nfe) {
						Log.e(LOG_TAG, nfe.getMessage(), nfe);
					}
					break;
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.getMessage(), ex);
		}
		return value;
	}

	/**
	 * 获取当前的电流值
	 * 
	 * @param file
	 * @param convertToMillis
	 * @return
	 */
	public Long getCurrentValue(File file, boolean convertToMillis) {
		Log.d(LOG_TAG, "*** file path ***" + file.getAbsolutePath());
		Log.d(LOG_TAG, "*** getCurrentValue ***");
		Log.d(LOG_TAG, "*** " + convertToMillis + " ***");
		String text = null;
		try {
			FileInputStream fs = new FileInputStream(file);
			DataInputStream ds = new DataInputStream(fs);
			text = ds.readLine();
			ds.close();
			fs.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Long value = null;

		if (text != null) {
			try {
				value = Long.parseLong(text);
			} catch (NumberFormatException nfe) {
				value = null;
			}
			if (convertToMillis)
				value = value / 1000;
		}
		return value;
	}
}
