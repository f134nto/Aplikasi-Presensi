package com.example.data

data class Teacher(
    val id: Int,
    val name: String,
    val nip: String = "",
    val role: String = "Guru", // "Guru" or "Karyawan"
    val department: String = "Pendidik"
)

object TeacherData {
    val SCHOOL_NAME = "MTs Ma'arif NU 1 Wangon"
    val TARGET_LAT = -7.50459941616919
    val TARGET_LNG = 109.06233985805635
    val MAX_RADIUS_METERS = 100.0

    val teacherList = listOf(
        Teacher(1, "Ahlan, S.Ag", "", "Guru", "Mapel PAI"),
        Teacher(2, "S.Enny Musrifa M,S.Pd", "NIP. 197512162005012000", "Guru", "Bahasa Indonesia"),
        Teacher(3, "Siti Mukromah,S.Ag.", "NIP. 197711262007012000", "Guru", "Aqidah Akhlak"),
        Teacher(4, "Retno Susilowati,S.H", "NIP. 197603282007102000", "Guru", "PPKn"),
        Teacher(5, "Latifatul Munawaroh, S.Pd.I., M.Pd.", "NIP. 198003162007102003", "Guru", "Bahasa Arab"),
        Teacher(6, "Rita Rosdiana,S.Pd", "NIP. 197812092007102002", "Guru", "Bahasa Inggris"),
        Teacher(7, "Ipung Purwanti,ST", "NIP. 197909052007102000", "Guru", "IPA"),
        Teacher(8, "Laela Nur Latifah, S. Pd", "NIP. 198202112007012005", "Guru", "Matematika"),
        Teacher(9, "Srie Kuntari,S.Pd", "", "Guru", "IPS"),
        Teacher(10, "Imam Zaenudin,S.Pd.I", "", "Guru", "Al-Qur'an Hadits"),
        Teacher(11, "Samsudin,S.H.I", "", "Guru", "Fiqih"),
        Teacher(12, "Sugiro,S.Pd.I", "", "Guru", "SKI"),
        Teacher(13, "Slamet,S.Pd", "", "Guru", "Penjasorkes"),
        Teacher(14, "Winarsih,S.Ag", "", "Guru", "Seni Budaya"),
        Teacher(15, "Karsono,S.Pt", "", "Guru", "Prakarya"),
        Teacher(16, "Waniti,S.Ag", "", "Guru", "Ke-NU-an"),
        Teacher(17, "Fitrianto Puji Pangarso, S.Kom.", "", "Guru", "Informatika / TIK"),
        Teacher(18, "Purbayu Budi Santosa, S.Pd", "", "Guru", "Bahasa Jawa"),
        Teacher(19, "Siti Lestari,S.Pd.", "", "Guru", "Bahasa Indonesia"),
        Teacher(20, "Wahyu Ushulludin, S.Th.I", "", "Guru", "PAI"),
        Teacher(21, "Aziz Kuntoro, S.Pd.", "", "Guru", "Matematika"),
        Teacher(22, "Kurniawan Aji Sularsono, S.Pd", "", "Guru", "Penjasorkes"),
        Teacher(23, "Istikowati,S.Pd", "", "Guru", "IPA"),
        Teacher(24, "SAFRIAH, S.Pd", "", "Guru", "Bahasa Inggris"),
        Teacher(25, "Agus Rahmat,S.H.I", "", "Guru", "Fiqih"),
        Teacher(26, "Lia Soviani,S.Pd", "", "Guru", "IPS"),
        Teacher(27, "Umi Habibah, S.Pd.I.", "", "Guru", "Aqidah Akhlak"),
        Teacher(28, "Arif Irfanudin,S.Pd", "", "Guru", "Bimbingan Konseling"),
        Teacher(29, "Emi Astuti, SE", "", "Karyawan", "Tata Usaha / Keuangan"),
        Teacher(30, "Fatma Hidayatutsani,S.Pd", "", "Guru", "Bahasa Indonesia"),
        Teacher(31, "Endah Lesatari, S.Pd.I.", "", "Guru", "PAI"),
        Teacher(32, "Asep Sugianto S.Pd", "", "Guru", "Matematika"),
        Teacher(33, "Yogi Bayun Wiasih, S. Pd. I", "", "Guru", "Ke-NU-an"),
        Teacher(34, "Farid Wujdi, S.Si", "", "Guru", "IPA Fisika"),
        Teacher(35, "Ramadhenthy Ahlan, S.Pd", "", "Guru", "Bahasa Inggris"),
        Teacher(36, "Leonita Pramuda Wardani, S.Pd.", "", "Guru", "Seni Budaya"),
        Teacher(37, "Arif Puji Santoso, S.Pd.", "", "Guru", "Informatika"),
        Teacher(38, "Hanifah, S.Pd", "", "Guru", "IPS"),
        Teacher(39, "Putri Hasna Nida Nandini, S.Pd.", "", "Guru", "Bahasa Indonesia"),
        Teacher(40, "Laili Latifah, S.Pd.", "", "Guru", "Matematika"),
        Teacher(41, "Muh. Muhafid", "", "Karyawan", "Staf Administrasi"),
        Teacher(42, "Nurrohmah", "", "Karyawan", "Perpustakaan"),
        Teacher(43, "Lely Subhan", "", "Karyawan", "Staf TU"),
        Teacher(44, "Samingun, S.AP", "", "Karyawan", "Kepala Tata Usaha"),
        Teacher(45, "Danang Setiawan HP", "", "Karyawan", "Teknisi IT / Operator"),
        Teacher(46, "Muhyidin", "", "Karyawan", "Keamanan / Satpam"),
        Teacher(47, "Maksum Slamet", "", "Karyawan", "Kebersihan & Sarpras"),
        Teacher(48, "Mustofa", "", "Karyawan", "Sarana Prasarana"),
        Teacher(49, "Iswandi", "", "Karyawan", "Staf Keuangan")
    )
}
