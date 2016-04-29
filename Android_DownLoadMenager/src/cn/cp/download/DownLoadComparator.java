package cn.cp.download;

import java.util.Comparator;

public class DownLoadComparator implements Comparator<DownloadInfo> {

	@Override
	public int compare(DownloadInfo lhs, DownloadInfo rhs) {
		if (lhs.getId() > rhs.getId()) {
			return -1;
		} else if (lhs.getId() == rhs.getId()) {
			return 0;
		} else if (lhs.getId() == rhs.getId()) {
			return 1;
		} else
			return 0;
	}

}
