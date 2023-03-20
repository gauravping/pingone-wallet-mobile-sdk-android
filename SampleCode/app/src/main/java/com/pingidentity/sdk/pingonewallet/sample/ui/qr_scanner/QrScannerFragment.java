package com.pingidentity.sdk.pingonewallet.sample.ui.qr_scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.callbacks.QrScannerCallback;
import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentQrScannerBinding;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class QrScannerFragment extends Fragment {

    public static final String TAG = QrScannerFragment.class.getCanonicalName();

    private static final float FRAME_PADDING = 10f;

    private float scaleX = 1f;
    private float scaleY = 1f;

    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    private BarcodeBoxView mBarcodeBoxView;
    private FragmentQrScannerBinding mBinding;
    private QrScannerCallback mListener;

    private final ActivityResultLauncher<String> permissionResult =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if (Boolean.TRUE.equals(result)) {
                    bindCameraUseCases();
                } else {
                    mListener.onCanceled();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });

    public static QrScannerFragment newInstance(QrScannerCallback listener) {
        QrScannerFragment dialogFragment = new QrScannerFragment();
        dialogFragment.mListener = listener;
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_qr_scanner, container, false);
        mBarcodeBoxView = new BarcodeBoxView(requireContext());
        mBinding.viewContainer.addView(mBarcodeBoxView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        checkPermission();
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mCameraProviderFuture.get().unbindAll();
        } catch (ExecutionException e) {
            Log.e(TAG, "Failed to unbind camera.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Action interrupted.", e);
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bindCameraUseCases();
        } else {
            permissionResult.launch(Manifest.permission.CAMERA);
        }
    }

    //////////////////////////////////////////
    ///////// Camera Works ///////////////////
    //////////////////////////////////////////

    private void bindCameraUseCases() {
        mCameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity());
        mCameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = mCameraProviderFuture.get();
                Preview previewUseCase = new Preview.Builder().build();
                ImageAnalysis analysisUseCase = getAnalysisUseCase();
                previewUseCase.setSurfaceProvider(mBinding.previewView.getSurfaceProvider());
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.bindToLifecycle(QrScannerFragment.this, cameraSelector, previewUseCase, analysisUseCase);
            } catch (ExecutionException e) {
                Log.e(TAG, "Failed to bind camera", e);
                mListener.onError(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Action interrupted.", e);
                mListener.onError(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private ImageAnalysis getAnalysisUseCase() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        ImageAnalysis analysisUseCase = new ImageAnalysis.Builder().build();
        analysisUseCase.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                imageProxy -> processImageProxy(scanner, imageProxy, analysisUseCase)
        );
        return analysisUseCase;
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(BarcodeScanner barcodeScanner, ImageProxy imageProxy, ImageAnalysis analysisUseCase) {
        if (imageProxy.getImage() == null) return;
        Image image = imageProxy.getImage();
        InputImage inputImage = InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees());
        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> processBarcode(barcodes, analysisUseCase, inputImage))
                .addOnFailureListener(throwable -> {
                    Log.e(TAG, "Failed to process barcode.", throwable);
                    mListener.onError(throwable);
                })
                .addOnCompleteListener(task -> {
                    imageProxy.getImage().close();
                    imageProxy.close();
                });
    }

    //////////////////////////////////////////
    ///////// Process Barcode ////////////////
    //////////////////////////////////////////

    private void processBarcode(List<Barcode> barcodes, ImageAnalysis analysisUseCase, InputImage inputImage) {
        if (barcodes == null || barcodes.isEmpty()) {
            mBarcodeBoxView.setRect(new RectF());
            return;
        }
        Barcode barcode = barcodes.get(0);
        if (barcode != null && barcode.getRawValue() != null) {
            scaleX = mBinding.previewView.getWidth() / (float) inputImage.getHeight();
            scaleY = mBinding.previewView.getHeight() / (float) inputImage.getWidth();
            mBarcodeBoxView.setRect(adjustBoundingRect(Objects.requireNonNull(barcode.getBoundingBox())));
            try {
                mCameraProviderFuture.get().unbind(analysisUseCase);
                stepDelay(() -> {
                    mListener.onQrScanned(barcode.getRawValue());
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            } catch (ExecutionException e) {
                Log.e(TAG, "Failed to bind camera", e);
                mListener.onError(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Action interrupted.", e);
                mListener.onError(e);
            }
        }
    }

    //////////////////////////////////////////
    ///////// QR Code frame //////////////////
    //////////////////////////////////////////

    private RectF adjustBoundingRect(Rect rect) {
        return new RectF(
                translateX(rect.left - FRAME_PADDING),
                translateY(rect.top - FRAME_PADDING),
                translateX(rect.right + FRAME_PADDING),
                translateY(rect.bottom + FRAME_PADDING)
        );
    }

    private Float translateX(Float x) {
        return x * scaleX;
    }

    private Float translateY(Float y) {
        return y * scaleY;
    }

    private void stepDelay(Runnable runnable) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                requireActivity().runOnUiThread(runnable);
            }
        }, 1000);
    }

}
