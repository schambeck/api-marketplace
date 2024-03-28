package com.schambeck.marketplace.usecase.shoppingcart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FileUploadResponse {
	private final String objectId;
	private final String fileName;
	private final String contentType;
	private final long size;
}
