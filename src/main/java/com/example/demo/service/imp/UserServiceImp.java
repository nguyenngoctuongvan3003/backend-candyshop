package com.example.demo.service.imp;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.AddressRequestDTO;
import com.example.demo.dto.AddressRequestUpdateDTO;
import com.example.demo.dto.ChangeEmailRequestDTO;
import com.example.demo.dto.ChangePasswordRequestDTO;
import com.example.demo.dto.UserProfileRequestDTO;
import com.example.demo.dto.VerifyUserRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.AuthenticationException;
import com.example.demo.model.Address;
import com.example.demo.model.District;
import com.example.demo.model.Province;
import com.example.demo.model.User;
import com.example.demo.model.Ward;
import com.example.demo.model.enums.Gender;
import com.example.demo.model.enums.UserStatus;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.DistrictRepository;
import com.example.demo.repository.ProvinceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WardRepository;
import com.example.demo.service.RedisService;
import com.example.demo.service.S3Service;
import com.example.demo.service.UserService;

@Service
public class UserServiceImp implements UserService {

	private UserRepository userRepository;
	private BCryptPasswordEncoder bycryptPasswordEncoder;
	private S3Service s3Service;
	private RedisService redisService;
	private ProvinceRepository provinceRepository;
	private DistrictRepository districtRepository;
	private WardRepository wardRepository;
	private AddressRepository addressRepository;
	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	public UserServiceImp(UserRepository userRepository, BCryptPasswordEncoder bycryptPasswordEncoder,
			S3Service s3Service, RedisService redisService, ProvinceRepository provinceRepository,
			DistrictRepository districtRepository, WardRepository wardRepository, AddressRepository addressRepository) {
		this.userRepository = userRepository;
		this.bycryptPasswordEncoder = bycryptPasswordEncoder;
		this.s3Service = s3Service;
		this.redisService = redisService;
		this.provinceRepository = provinceRepository;
		this.districtRepository = districtRepository;
		this.wardRepository = wardRepository;
		this.addressRepository = addressRepository;
	}

	@Override
	@Transactional
	public User updateUser(String userId, UserProfileRequestDTO profileRequestDTO)
			throws Exception, ResourceNotFoundException {

		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// if (userRepository.existsByPhoneNumber(profileRequestDTO.getPhoneNumber()))
		// 	throw new ResourceConflictException("phoneNumber", "Phone number already");

		String firstName = profileRequestDTO.getFirstName();
		String lastName = profileRequestDTO.getLastName();
		String phoneNumber = profileRequestDTO.getPhoneNumber();
		String gender = profileRequestDTO.getGender();
		LocalDate birthDay = profileRequestDTO.getBirthDay();

		if (firstName != null)
			user.setFirstName(firstName);
		if (lastName != null)
			user.setLastName(lastName);
		if (phoneNumber != null)
			user.setPhoneNumber(phoneNumber);
		if (gender != null)
			user.setGender(Gender.valueOf(profileRequestDTO.getGender()));
		if (birthDay != null)
			user.setBirthDay(profileRequestDTO.getBirthDay());

		return userRepository.save(user);
	}

	@Override
	@Transactional
	public void changePassword(String userId, ChangePasswordRequestDTO changePasswordRequestDTO)
			throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		String oldPassword = changePasswordRequestDTO.getOldPassword();
		String newPassword = changePasswordRequestDTO.getNewPassword();

		if (!bycryptPasswordEncoder.matches(oldPassword, user.getPassword()))
			throw new AuthenticationException("Old password is incorrect");
		user.setPassword(bycryptPasswordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	@Override
	@Transactional
	public User uploadAvatar(String userId, MultipartFile multipartFile) throws Exception {
		if (multipartFile == null)
			throw new BadRequestException("avatar", "Avatar is required");
		String avatarName = null;
		try {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new ResourceNotFoundException("User not found"));
			if (user.getAvatar() != null && user.getAvatarUrl() != null)
				s3Service.deleteFile(user.getAvatar());
			avatarName = s3Service.uploadFile(multipartFile);
			String avatarUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, "ap-southeast-1",
					avatarName);
			user.setAvatar(avatarName);
			user.setAvatarUrl(avatarUrl);
			return userRepository.save(user);
		} catch (Exception e) {
			if (avatarName != null)
				s3Service.deleteFile(avatarName);
			throw e;
		}
	}

	@Override
	@Transactional
	public User changeEmail(String userId, ChangeEmailRequestDTO changeEmailRequestDTO)
			throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (userRepository.existsByEmail(changeEmailRequestDTO.getNewEmail()))
			throw new ResourceConflictException("email", "Email already exists");
		if (!bycryptPasswordEncoder.matches(changeEmailRequestDTO.getPassword(), user.getPassword()))
			throw new AuthenticationException("Password is incorrect");
		Object otp = redisService.get(String.format("otp?email=%s", changeEmailRequestDTO.getNewEmail()));
		if (otp == null)
			throw new AuthenticationException("OTP is incorrect or expired");
		if (!otp.equals(changeEmailRequestDTO.getOtp()))
			throw new AuthenticationException("OTP is incorrect or expired");
		redisService.delete(String.format("otp?email=%s", changeEmailRequestDTO.getNewEmail()));
		user.setEmail(changeEmailRequestDTO.getNewEmail());
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public Address createAddress(String userId, AddressRequestDTO address) throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Province province = provinceRepository.findById(address.getProvinceId())
				.orElseThrow(() -> new ResourceNotFoundException("Province not found"));
		District district = districtRepository.findById(address.getDistrictId())
				.orElseThrow(() -> new ResourceNotFoundException("District not found"));
		Ward ward = wardRepository.findById(address.getWardId())
				.orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
		Address newAddress = new Address();
		newAddress.setAddress(address.getAddress());
		newAddress.setCustomerName(address.getCustomerName());
		newAddress.setPhoneNumber(address.getPhoneNumber());
		newAddress.setProvince(province);
		newAddress.setDistrict(district);
		newAddress.setWard(ward);
		newAddress.setUser(user);
		return addressRepository.save(newAddress);
	}

	@Override
	@Transactional
	public Address updateAddress(String userId, String addressId, AddressRequestUpdateDTO address)
			throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Province province = null;
		if (address.getProvinceId() != null)
			province = provinceRepository.findById(address.getProvinceId())
					.orElseThrow(() -> new ResourceNotFoundException("Province not found"));
		District district = null;
		if (address.getDistrictId() != null)
			district = districtRepository.findById(address.getDistrictId())
					.orElseThrow(() -> new ResourceNotFoundException("District not found"));
		Ward ward = null;
		if (address.getWardId() != null)
			ward = wardRepository.findById(address.getWardId())
					.orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
		Address oldAddress = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
		if (!oldAddress.getUser().getUserId().equals(user.getUserId()))
			throw new AuthenticationException("Unauthorized");
		if (address.getAddress() != null)
			oldAddress.setAddress(address.getAddress());
		if (address.getCustomerName() != null)
			oldAddress.setCustomerName(address.getCustomerName());
		if (address.getPhoneNumber() != null)
			oldAddress.setPhoneNumber(address.getPhoneNumber());
		if (address.getProvinceId() != null)
			oldAddress.setProvince(province);
		if (address.getDistrictId() != null)
			oldAddress.setDistrict(district);
		if (address.getWardId() != null)
			oldAddress.setWard(ward);
		return addressRepository.save(oldAddress);
	}

	@Override
	@Transactional
	public void deleteAddress(String userId, String addressId) throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Address address = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
		if (!address.getUser().getUserId().equals(user.getUserId()))
			throw new AuthenticationException("Unauthorized");
		addressRepository.delete(address);
	}

	@Override
	public Address getAddress(String userId, String addressId) throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Address address = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
		if (!address.getUser().getUserId().equals(user.getUserId()))
			throw new AuthenticationException("Unauthorized");
		return address;
	}

	@Override
	public List<Address> getAddresses(String userId) throws Exception, ResourceNotFoundException {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getAddresses();
	}

	@Override
	@Transactional
	public User verifyUser(String userId, VerifyUserRequest verifyUserRequest) throws Exception {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		String email = user.getEmail();
		Object savedOTP = redisService.get(String.format("otp?email=%s", email));
		if (savedOTP == null || !savedOTP.equals(verifyUserRequest.getOtp()))
			throw new AuthenticationException("OTP is invalid or expired");
		if (user.getStatus().equals(UserStatus.INACTIVE)) {
			user.setStatus(UserStatus.ACTIVE);
		} else {
			throw new AuthenticationException("User is already verified");
		}
		redisService.delete(String.format("otp?email=%s", email));
		return userRepository.save(user);
	}

	@Override
	public User getUserById(String userId) {
		return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

}
