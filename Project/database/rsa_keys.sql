-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th1 10, 2025 lúc 12:50 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `rsa_key`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `rsa_keys`
--

CREATE TABLE `rsa_keys` (
  `id` int(11) NOT NULL,
  `key_name` varchar(255) NOT NULL,
  `public_key` blob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `rsa_keys`
--

INSERT INTO `rsa_keys` (`id`, `key_name`, `public_key`) VALUES
(1, 'default_key', 0x30819f300d06092a864886f70d010101050003818d0030818902818100e1ecf6c78d0f730fd86af61ad2cfd27eca121507163cdffbea44e9c86fc4009035707740bbc012b7fa9c7ab142f65c79f97c912568521b111cdbdfef253aec0f99cbd0e7c8dc5e1cd6c31459e98f021d1c30e9811510798d2911d56837a3c9a13d9dda6b996953da697404c3b372b2450e663582c4b717bfff58f20fc9bad4050203010001);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `rsa_keys`
--
ALTER TABLE `rsa_keys`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `key_name` (`key_name`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `rsa_keys`
--
ALTER TABLE `rsa_keys`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
